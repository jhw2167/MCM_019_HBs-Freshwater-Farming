package com.holybuckets.farming.mixin;

import com.holybuckets.farming.config.ModConfig;
import com.holybuckets.farming.core.CropGrowthManager;
import com.holybuckets.farming.core.ManagedPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttachedStemBlock.class)
public abstract class MixinAttachedStemBlock {

    /** Vanilla reverts a stem that lost its fruit to max age; drop it to stemPlantResetAge instead. */
    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void hbs_farming$resetStemAge(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                          BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState result = cir.getReturnValue();
        if (result == state) return;

        ManagedPlant plant = CropGrowthManager.getPlant(result.getBlock());
        if (plant == null || plant.getType() != ManagedPlant.Type.STEM) return;

        int resetAge = Math.min(ModConfig.getInstance().getStemPlantResetAge(), plant.getMaxAge());
        cir.setReturnValue(result.setValue(plant.getAge(), resetAge));
    }

}
