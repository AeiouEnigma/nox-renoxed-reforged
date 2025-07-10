/*
 * -------------------------------------------------------------------
 * Nox
 * Copyright (c) 2025 SciRave
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * -------------------------------------------------------------------
 */

package net.scirave.nox.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MagmaCube.class)
public abstract class MagmaCubeEntityMixin extends SlimeEntityMixin {

    private static final BlockState nox$LAVA_SOURCE = Blocks.LAVA.defaultBlockState();
    private static final BlockState nox$FLOWING_LAVA = Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, 8);
    private static final BlockState nox$SMALL_LAVA = Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7);

    @Inject(method = "getJumpDelay", at = @At("HEAD"), cancellable = true)
    private void nox$makeMagmaCubesJumpConstantly(CallbackInfoReturnable<Integer> cir) {
        if (NoxConfig.slimesJumpConstantly)
            cir.setReturnValue(4);
    }

    @Override
    public void nox$slimeOnAttack(LivingEntity victim, CallbackInfo ci) {
        if (NoxConfig.magmaCubeAttacksIgniteTarget)
            victim.igniteForSeconds(NoxConfig.magmaCubeContactFireDuration);
    }

    private void nox$attemptLavaFill(BlockPos pos) {
        if (!this.level().isClientSide() && NoxConfig.magmaCubeLeavesLavaWhenKilled && this.level().getBlockState(pos).canBeReplaced()) {
            this.level().setBlockAndUpdate(pos, NoxConfig.magmaCubeMakesLavaSourceBlocks ? nox$LAVA_SOURCE : nox$FLOWING_LAVA);
        }
    }

    private void nox$attemptSmallLavaFill(BlockPos pos) {
        // Used for aesthetics when magmaCubeMakesLavaSourceBlocks is false
        if (!this.level().isClientSide() && this.level().getBlockState(pos).canBeReplaced())
            this.level().setBlockAndUpdate(pos, nox$SMALL_LAVA);
    }

    @Override
    public void nox$slimeOnDeath() {
        if (NoxConfig.magmaCubeLeavesLavaWhenKilled) {
            BlockPos origin = this.blockPosition();
            nox$attemptLavaFill(origin);
            int size = this.getSize();
            if (size < 2) {
                if (!NoxConfig.magmaCubeMakesLavaSourceBlocks)
                    nox$attemptSmallLavaFill(origin.above());
            }
            else {
                if (NoxConfig.magmaCubeMakesLavaSourceBlocks)
                    nox$attemptLavaFill(origin.above());
                else
                    nox$attemptSmallLavaFill(origin.above());
                nox$attemptLavaFill(origin.below());
                nox$attemptLavaFill(origin.north());
                nox$attemptLavaFill(origin.south());
                nox$attemptLavaFill(origin.east());
                nox$attemptLavaFill(origin.west());
                if (size >= 4) {
                    nox$attemptLavaFill(origin.above().north());
                    nox$attemptLavaFill(origin.above().south());
                    nox$attemptLavaFill(origin.above().east());
                    nox$attemptLavaFill(origin.above().west());
                    nox$attemptLavaFill(origin.below().north());
                    nox$attemptLavaFill(origin.below().south());
                    nox$attemptLavaFill(origin.below().east());
                    nox$attemptLavaFill(origin.below().west());
                    nox$attemptLavaFill(origin.north().east());
                    nox$attemptLavaFill(origin.north().west());
                    nox$attemptLavaFill(origin.south().east());
                    nox$attemptLavaFill(origin.south().west());
                }
            }
        }
    }


}
