package com.holybuckets.farming.mixin;

import com.holybuckets.farming.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmBlock.class)
public abstract class MixinFarmBlock {

    /** Same 9x2x9 range as vanilla; falls through to the vanilla water check when no alternate block is found. */
    @Inject(method = "isNearWater", at = @At("HEAD"), cancellable = true)
    private static void hbs_farming$isNearAlternateFreshwater(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ModConfig config = ModConfig.getInstance();
        if (!config.hasAlternateFreshwater()) return;

        for (BlockPos checked : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (config.isAlternateFreshwater(level.getBlockState(checked).getBlock())) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

}
