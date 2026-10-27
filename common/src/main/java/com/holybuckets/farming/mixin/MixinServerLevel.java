package com.holybuckets.farming.mixin;

import com.holybuckets.farming.core.CropGrowthManager;
import com.holybuckets.farming.core.ManagedPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    @Unique
    private CropGrowthManager hbs_farming$cropGrowthManager;

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void hbs_farming$onTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        CropGrowthManager manager = hbs_farming$getManager();
        if (manager != null) manager.onChunkTick(chunk, randomTickSpeed);
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void hbs_farming$onBlockRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ManagedPlant plant = CropGrowthManager.getPlant(state.getBlock());
        CropGrowthManager manager;
        if (plant == null || (manager = hbs_farming$getManager()) == null) {
            state.randomTick(level, pos, random);
            return;
        }
        if (manager.allowRandomTick(plant, pos, random)) {
            state.randomTick(level, pos, random);
        }
    }

    @Unique
    private CropGrowthManager hbs_farming$getManager() {
        if (hbs_farming$cropGrowthManager == null) {
            hbs_farming$cropGrowthManager = CropGrowthManager.get((ServerLevel) (Object) this);
        }
        return hbs_farming$cropGrowthManager;
    }

}
