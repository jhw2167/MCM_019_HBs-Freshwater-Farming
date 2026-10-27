package com.holybuckets.farming.core;

import com.holybuckets.farming.LoggerProject;
import com.holybuckets.farming.config.FreshwaterFarmingConfig;
import com.holybuckets.farming.config.ModConfig;
import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.PlayerInteractEvent;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.blay09.mods.balm.api.event.UseBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralized class for managing crop growth rate in different biomes for different crops, one instance per level.
 * Growth itself is vanilla; this class only decides how many random ticks reach each plant.
 */
public class CropGrowthManager {

    private static final String CLASS_ID = "003";

    /** Wheat on ideal farmland: 7 stages at a 1 in 3 chance per random tick.
    *   Recall random ticks happen to one block per section of ~4k blocks
    *   */
    public static final float VANILLA_RANDOM_TICKS_TO_MATURE = 21f;
    public static final int TICKS_PER_MINUTE = 1200;
    public static final long CATCH_UP_THRESHOLD_TICKS = 1200;

    public static final float FULL_GROWTH_FACTOR = 1.5f;
    private static final float SECTION_BLOCKS = 4096f;

    //** STATICS
    static final Map<LevelAccessor, CropGrowthManager> MANAGERS = new HashMap<>();
    private static final Reference2ObjectOpenHashMap<Block, ManagedPlant> PLANTS = new Reference2ObjectOpenHashMap<>();
    private static final Set<Item> PLANT_ITEMS = new ReferenceOpenHashSet<>();
    private static final Reference2ObjectOpenHashMap<Item, ManagedPlant> PLANTS_BY_ITEM = new Reference2ObjectOpenHashMap<>();
    private static FreshwaterFarmingConfig GENERAL_CONFIG;
    private static ModConfig MOD_CONFIG;

    //** INSTANCE
    private final ServerLevel level;
    private final FreshwaterFarmingConfig generalConfig;
    private final ModConfig modConfig;
    private final Long2ObjectOpenHashMap<ManagedCropChunk> cropChunks = new Long2ObjectOpenHashMap<>();

    private final long targetGrowthTicks;
    private final long fullGrowthTicks;
    private int cachedRandomTickSpeed = -1;
    private float baseTickChance;
    private float catchUpTicksPerTick;
    private boolean currentChunkCatchingUp;
    private long cachedGameTime = Long.MIN_VALUE;
    private long cachedGrowthTime;


    private CropGrowthManager(ServerLevel level, FreshwaterFarmingConfig generalConfig, ModConfig modConfig) {
        this.level = level;
        this.generalConfig = generalConfig;
        this.modConfig = modConfig;
        this.targetGrowthTicks = (long) generalConfig.standardGrowthTimeMins * TICKS_PER_MINUTE;
        this.fullGrowthTicks = (long) (targetGrowthTicks * FULL_GROWTH_FACTOR);
        LoggerProject.logInit(CLASS_ID + "000", this.getClass().getName());
    }


    //** LIFECYCLE, called in order by FreshwaterFarmingMain
    public static void init(EventRegistrar registrar) {
        ManagedCropChunk.registerManagedChunkData();
        registrar.registerOnPlayerInteract(PlayerInteractEvent.RightClickInteraction.class, CropGrowthManager::onUseBlock);
    }

    public static void onServerStarting(MinecraftServer server, FreshwaterFarmingConfig generalConfig, ModConfig modConfig) {
        GENERAL_CONFIG = generalConfig;
        MOD_CONFIG = modConfig;
        MANAGERS.clear();
        buildPlantTable();
    }

    public static void onLevelLoad(ServerLevel level) {
        if (GENERAL_CONFIG == null || MOD_CONFIG == null) return;
        MANAGERS.computeIfAbsent(level, l -> new CropGrowthManager(level, GENERAL_CONFIG, MOD_CONFIG));
    }

    public static void onLevelUnload(LevelAccessor level) {
        MANAGERS.remove(level);
    }

    public static void onServerStopped() {
        MANAGERS.clear();
        PLANTS.clear();
        PLANT_ITEMS.clear();
        PLANTS_BY_ITEM.clear();
        GENERAL_CONFIG = null;
        MOD_CONFIG = null;
    }

    // Allows quick lookup of ManagedPlant by block or seed item
    private static void buildPlantTable() {
        PLANTS.clear();
        PLANT_ITEMS.clear();
        PLANTS_BY_ITEM.clear();
        for (Block block : BuiltInRegistries.BLOCK) {
            ManagedPlant plant = ManagedPlant.classify(block);
            if (plant == null) continue;
            PLANTS.put(block, plant);
            PLANT_ITEMS.add(plant.getSeed());
            PLANTS_BY_ITEM.putIfAbsent(plant.getSeed(), plant);
        }
        LoggerProject.logInfo(CLASS_ID + "001", "Managing growth for " + PLANTS.size() + " plant block(s)");
    }


    //** STATIC ACCESSORS

    @Nullable
    public static CropGrowthManager get(LevelAccessor level) {
        return MANAGERS.get(level);
    }

    @Nullable
    public static ManagedPlant getPlant(Block block) {
        return PLANTS.get(block);
    }

    /** Every item that plants a managed plant, as resolved at server start. */
    public static Set<Item> getManagedSeeds() {
        return java.util.Collections.unmodifiableSet(PLANT_ITEMS);
    }

    public static boolean isManagedPlant(BlockState state) {
        return PLANTS.containsKey(state.getBlock());
    }


    //** EVENTS

    private static void onUseBlock(PlayerInteractEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        Item held = event.getPlayer().getItemInHand(event.getHand()).getItem();
        if (!PLANT_ITEMS.contains(held)) return;

        CropGrowthManager manager = MANAGERS.get(event.getLevel());
        if (manager == null) return;

        BlockPos placePos = event.getPos();
        manager.designateCropChunk(new ChunkPos(placePos));
        manager.remindIfNoSkylight(event.getPlayer(), held, placePos);
        String itemName = HBUtil.ItemUtil.itemToString(held);
        float modifier = manager.getGrowthModifier(placePos, held);
        LoggerProject.logInfo("003002", "Placed: " + itemName + " with crop growth speed "+ modifier);
    }

        private void remindIfNoSkylight(Player player, Item seed, BlockPos placePos) {
            if (modConfig.ignoresSkylight(seed) || level.canSeeSky(placePos)) return;

            ManagedPlant plant = PLANTS_BY_ITEM.get(seed);
            if (plant == null || !level.getBlockState(placePos).canBeReplaced()) return;
            if (!plant.getBlock().defaultBlockState().canSurvive(level, placePos)) return;

            Messager.getInstance().sendBottomActionHint(player, seed.getDescription().getString() + " needs access to the sky to grow");
        }

    /** From the head of ServerLevel#tickChunk; the random ticks that follow belong to this chunk. */
    private Map<Item,Float> cachedSeedGrowthModifier;
    public void onChunkTick(LevelChunk chunk, int randomTickSpeed)
    {
        if (randomTickSpeed != cachedRandomTickSpeed) {
            cachedRandomTickSpeed = randomTickSpeed;
            updateRandomTickSpeed(randomTickSpeed);
        }
        currentChunkCatchingUp = false;
        cachedSeedGrowthModifier = MOD_CONFIG.getGrowthModifier(level.getBiome(chunk.getPos().getWorldPosition()));
        if (cropChunks.isEmpty() || randomTickSpeed <= 0) return;

        ManagedCropChunk cropChunk = cropChunks.get(chunk.getPos().toLong());
        if (cropChunk == null) return;
        currentChunkCatchingUp = cropChunk.onChunkTick(this, chunk, getGrowthTime());
    }

    /**
     * Chief method for slowing native Random Tick Rate which triggers plant growth
     *
     */
    public boolean allowRandomTick(ManagedPlant plant, BlockPos pos, RandomSource random) {

        float modifier = cachedSeedGrowthModifier.getOrDefault(plant.getSeed(), 1f);
        if (modifier <= 0f) return false;

        if(currentChunkCatchingUp) {
            //lego
        } else if(random.nextFloat() <= baseTickChance*modifier) {
            //tick chance rate is greater, continue
        } else {
            return false;
        }

        if (!hasSkylightAccess(plant, pos)) return false;
        return canGrow(plant, pos);
    }


    //** RATES

    /** Elapsed ticks beyond which a returning player sees plants jump to fully grown. */
    public long getFullGrowthTicks() {
        return fullGrowthTicks;
    }

    public static float getRandomTickInterval(int randomTickSpeed) {
        return SECTION_BLOCKS / randomTickSpeed;
    }

    public float getRandomTicksOffered(int randomTickSpeed) {
        return targetGrowthTicks / getRandomTickInterval(randomTickSpeed);
    }

    public float getCatchUpTicksPerTick() {
        return catchUpTicksPerTick;
    }

    private void updateRandomTickSpeed(int randomTickSpeed) {
        if (randomTickSpeed <= 0) {
            baseTickChance = 0f;
            catchUpTicksPerTick = 0f;
            return;
        }
        baseTickChance = VANILLA_RANDOM_TICKS_TO_MATURE / getRandomTicksOffered(randomTickSpeed);
        catchUpTicksPerTick = baseTickChance >= 1f ? 0f : 1f / baseTickChance - 1f;
    }

    /** Foundation's sleep-inclusive tick count, computed at most once per game tick. */
    public long getGrowthTime() {
        long gameTime = level.getGameTime();
        if (gameTime != cachedGameTime) {
            cachedGameTime = gameTime;
            try {
                cachedGrowthTime = GeneralConfig.getInstance().getTotalTickCountWithSleep(level);
            } catch (RuntimeException e) {
                cachedGrowthTime = level.getDayTime();
            }
        }
        return cachedGrowthTime;
    }

    /** Unlisted biome and seed pairings return the standard rate of 1. */
    public float getGrowthModifier(BlockPos pos, Item seed) {
        return modConfig.getGrowthModifier(level.getBiome(pos), seed);
    }


    //** CONDITIONS

    /** Open sky above the plant, unless its seed is listed in ignoreNeedsSkylight. */
    public boolean hasSkylightAccess(ManagedPlant plant, BlockPos pos) {
        return modConfig.ignoresSkylight(plant.getSeed()) || level.canSeeSky(pos);
    }

    private boolean canGrow(ManagedPlant plant, BlockPos pos) {
        if (!plant.needsFarmland()) return true;
        BlockPos soilPos = pos.below();
        BlockState soil = level.getBlockState(soilPos);
        return isSuitableSoil(soilPos, soil) && isHydrated(soilPos, soil);
    }

    public boolean isSuitableSoil(BlockPos soilPos, BlockState soilState) {
        //TODO
        return true;
    }

    public boolean isHydrated(BlockPos soilPos, BlockState soilState) {
        //TODO
        return true;
    }

    /**
     * Behavior supports players leaving their fields for a long period of time and
     * returning to a fully grown field.
     * Limited the fullGrowthTicks to its .80% threshold in case a player hangs out around the crops
     * for a bit and THEN leaves, so players will feel like its working properly.
     * @param plant
     * @param pos
     * @param elapsedTicks
     * @return
     */
    boolean checkForFullGrowth(ManagedPlant plant, BlockPos pos, long elapsedTicks) {
        if (!hasSkylightAccess(plant, pos)) return false;
        if (plant.needsLight() && getStaticLight(pos) < ManagedPlant.MIN_LIGHT) return false;
        float modifier = getGrowthModifier(pos, plant.getSeed());
        if (modifier <= 0f || elapsedTicks * modifier < fullGrowthTicks*0.8) return false;
        return canGrow(plant, pos);
    }

    private int getStaticLight(BlockPos pos) {
        return Math.max(level.getBrightness(LightLayer.SKY, pos), level.getBrightness(LightLayer.BLOCK, pos));
    }


    //** GROWTH

    public static void grow(ServerLevel level, Set<BlockPos> positions)
    {
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            ManagedPlant plant = PLANTS.get(state.getBlock());
            if (plant == null) continue;

            switch (plant.getType()) {
                case CROP, BUSH -> {
                    if (state.getValue(plant.getAge()) < plant.getMaxAge()) {
                        level.setBlock(pos, state.setValue(plant.getAge(), plant.getMaxAge()), Block.UPDATE_CLIENTS);
                    }
                }
                case STEM -> {
                    BlockState mature = state.setValue(plant.getAge(), plant.getMaxAge());
                    level.setBlock(pos, mature, Block.UPDATE_CLIENTS);
                    growFruit(level, plant, pos);
                }
                case COLUMN -> growColumn(level, plant, pos);
            }
        }
    }

    private static void growFruit(ServerLevel level, ManagedPlant plant, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos fruitPos = pos.relative(dir);
            if (!level.getBlockState(fruitPos).isAir()) continue;
            BlockState below = level.getBlockState(fruitPos.below());
            if (!below.is(Blocks.FARMLAND) && !below.is(BlockTags.DIRT)) continue;

            level.setBlockAndUpdate(fruitPos, plant.getFruit().defaultBlockState());
            level.setBlockAndUpdate(pos, plant.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dir));
            return;
        }
    }

    /** Only the top block of a column grows it, up to the vanilla height. */
    private static void growColumn(ServerLevel level, ManagedPlant plant, BlockPos pos) {
        BlockPos top = pos.above();
        if (!level.isEmptyBlock(top)) return;

        Block block = plant.getBlock();
        int height = 1;
        while (height < plant.getMaxAge() && level.getBlockState(pos.below(height)).is(block)) height++;

        BlockState grown = block.defaultBlockState();
        for (; height < plant.getMaxAge(); height++) {
            if (!level.isEmptyBlock(top) || !grown.canSurvive(level, top)) return;
            level.setBlockAndUpdate(top, grown);
            top = top.above();
        }
    }


    //** CROP CHUNKS

    public void designateCropChunk(ChunkPos pos) {
        long key = pos.toLong();
        if (cropChunks.containsKey(key)) return;
        ManagedCropChunk cropChunk = ManagedCropChunk.getOrCreate(level, pos);
        cropChunk.setRandomTick(getGrowthTime());
        cropChunks.put(key, cropChunk);
    }

    void addCropChunk(ManagedCropChunk cropChunk) {
        if (cropChunk.getChunkPos() == null) return;
        cropChunks.putIfAbsent(cropChunk.getChunkPos().toLong(), cropChunk);
    }

    void removeCropChunk(ManagedCropChunk cropChunk) {
        if (cropChunk.getChunkPos() == null) return;
        cropChunks.remove(cropChunk.getChunkPos().toLong());
    }

    @Nullable
    public ManagedCropChunk getCropChunk(ChunkPos pos) {
        return cropChunks.get(pos.toLong());
    }


    public ServerLevel getLevel() {
        return level;
    }

    public FreshwaterFarmingConfig getGeneralConfig() {
        return generalConfig;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

}
