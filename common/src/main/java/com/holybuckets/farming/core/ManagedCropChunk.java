package com.holybuckets.farming.core;

import com.holybuckets.foundation.HBUtil.ChunkUtil;
import com.holybuckets.foundation.model.ManagedChunk;
import com.holybuckets.foundation.model.ManagedChunkUtility;
import com.holybuckets.foundation.modelInterface.IMangedChunkData;
import net.blay09.mods.balm.api.event.ChunkLoadingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * A chunk the player has planted in; remembers when it last random ticked so growth missed while it was not ticking is owed back.
 */
public class ManagedCropChunk implements IMangedChunkData {

    private static final String NBT_LAST_RANDOM_TICK = "lastRandomTickTime";
    private static final String NBT_OWED_TICKS = "owedTicks";

    private LevelAccessor level;
    private String id;
    private ChunkPos pos;
    private long lastRandomTickTime;
    private float catchupTicksRemaining;

    public static void registerManagedChunkData() {
        ManagedChunk.registerManagedChunkData(ManagedCropChunk.class, ManagedCropChunk::new);
    }

    private ManagedCropChunk() {
        super();
    }

    private ManagedCropChunk(LevelAccessor level, ChunkPos pos) {
        this();
        this.level = level;
        this.pos = pos;
        this.id = ChunkUtil.getId(pos);
    }


    //** GETTERS

    public String getId() { return id; }

    public ChunkPos getChunkPos() { return pos; }

    //** CORE
    void setRandomTick(long now) {
        this.lastRandomTickTime = now;
    }

    //Called from servelLevel.randomTick mixin
    boolean onChunkTick(CropGrowthManager manager, LevelChunk chunk, long now)
    {
        long elapsed = now - lastRandomTickTime;
        lastRandomTickTime = now;
        chunk.setUnsaved(true);

        if (elapsed > CropGrowthManager.CATCH_UP_THRESHOLD_TICKS) {
            if (elapsed >= manager.getFullGrowthTicks() && !growToFull(manager, chunk, elapsed)) return false;
            catchupTicksRemaining = Math.min(catchupTicksRemaining + elapsed, manager.getFullGrowthTicks());
        }

        if (catchupTicksRemaining <= 0f) return false;
        float repaid = manager.getCatchUpTicksPerTick();
        if (repaid <= 0f) {
            catchupTicksRemaining = 0f;
            return false;
        }
        catchupTicksRemaining -= repaid;
        return true;
    }

    /**
     * Scans only randomly ticking sections whose palette holds a managed plant and jumps qualifying plants to fully grown.
     * Returns false and drops the chunk from the manager when no plants remain.
     */
    boolean growToFull(CropGrowthManager manager, LevelChunk chunk, long elapsedTicks)
    {
        Set<BlockPos> grow = new HashSet<>();
        boolean foundPlant = false;
        LevelChunkSection[] sections = chunk.getSections();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (!section.isRandomlyTicking()) continue;
            if (!section.getStates().maybeHas(CropGrowthManager::isManagedPlant)) continue;

            int minY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        ManagedPlant plant = CropGrowthManager.getPlant(section.getBlockState(x, y, z).getBlock());
                        if (plant == null) continue;
                        foundPlant = true;
                        BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
                        if (manager.checkForFullGrowth(plant, pos, elapsedTicks)) grow.add(pos);
                    }
                }
            }
        }

        if (!foundPlant) {
            catchupTicksRemaining = 0f;
            manager.removeCropChunk(this);
            return false;
        }
        CropGrowthManager.grow(manager.getLevel(), grow);
        return true;
    }

    //** STATICS

    /** Returns the instance attached to the chunk's ManagedChunk, creating and attaching one if needed. */
    static ManagedCropChunk getOrCreate(LevelAccessor level, ChunkPos pos) {
        String id = ChunkUtil.getId(pos);
        ManagedChunkUtility util = ManagedChunkUtility.getInstance(level);
        ManagedChunk parent = util == null ? null : util.getManagedChunk(id);
        if (parent != null && parent.getSubclass(ManagedCropChunk.class) instanceof ManagedCropChunk existing) {
            existing.level = level;
            return existing;
        }

        ManagedCropChunk created = new ManagedCropChunk(level, pos);
        if (parent != null) parent.setSubclass(ManagedCropChunk.class, created);
        return created;
    }


    //** IMangedChunkData

    @Override
    public void setId(String id) {
        if (id == null) return;
        this.id = id;
        this.pos = ChunkUtil.getChunkPos(id);
    }

    @Override
    public void setLevel(LevelAccessor level) {
        this.level = level;
    }

    @Override
    public IMangedChunkData resolveSubData(LevelAccessor level, String id, @Nullable IMangedChunkData data) {
        if (id == null || level == null || level.isClientSide()) return data;
        CropGrowthManager manager = CropGrowthManager.get(level);
        if (manager == null) return data;

        ManagedCropChunk inMemory = manager.getCropChunk(ChunkUtil.getChunkPos(id));
        if (inMemory != null) return inMemory;

        ManagedCropChunk serialized = (ManagedCropChunk) data;
        if (serialized != null) manager.addCropChunk(serialized);
        return serialized;
    }

    public IMangedChunkData getStaticInstance(LevelAccessor level, String id) {
        CropGrowthManager manager = CropGrowthManager.get(level);
        if (manager == null || id == null) return new ManagedCropChunk();
        ManagedCropChunk inMemory = manager.getCropChunk(ChunkUtil.getChunkPos(id));
        return inMemory != null ? inMemory : new ManagedCropChunk();
    }

    @Override
    public boolean isInit(String subClass) {
        return subClass.equals(ManagedCropChunk.class.getName()) && this.id != null;
    }

    @Override
    public void handleChunkLoaded(ChunkLoadingEvent.Load event) {
        this.level = event.getLevel();
        if (level.isClientSide()) return;
        CropGrowthManager manager = CropGrowthManager.get(level);
        if (manager != null) manager.addCropChunk(this);
    }

    @Override
    public void handleChunkUnloaded(ChunkLoadingEvent.Unload event) {
        if (level == null || level.isClientSide()) return;
        CropGrowthManager manager = CropGrowthManager.get(level);
        if (manager != null) manager.removeCropChunk(this);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (id == null) return tag;
        tag.putString("id", id);
        tag.putLong(NBT_LAST_RANDOM_TICK, lastRandomTickTime);
        tag.putFloat(NBT_OWED_TICKS, catchupTicksRemaining);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_LAST_RANDOM_TICK)) return;
        if (tag.contains("id")) setId(tag.getString("id"));
        this.lastRandomTickTime = tag.getLong(NBT_LAST_RANDOM_TICK);
        this.catchupTicksRemaining = tag.getFloat(NBT_OWED_TICKS);
    }

}
