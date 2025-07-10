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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Guardian.class)
public abstract class GuardianEntityMixin extends HostileEntityMixin {
    private static final BlockState nox$WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState nox$FLOWING_WATER = nox$WATER.setValue(BlockStateProperties.LEVEL, 8);
    private static final BlockState nox$SMALL_WATER = nox$WATER.setValue(BlockStateProperties.LEVEL, 7);

    @Override
    public void nox$onDamaged(DamageSource source, float amount, CallbackInfo ci) {
    if (NoxConfig.guardiansPlaceWaterOnDeath && !this.level().isClientSide()) {
            BlockPos pos = this.blockPosition();
            BlockState state = this.level().getBlockState(pos);
            if (state != nox$WATER && state.canBeReplaced()) {
                if (NoxConfig.guardianDeathLeavesWaterSource)
                    this.level().setBlockAndUpdate(pos, nox$WATER);
                else {
                    // order matters
                    state = this.level().getBlockState(pos.above());
                    this.level().setBlockAndUpdate(pos, nox$FLOWING_WATER);
                    if (state != nox$WATER && state.canBeReplaced())
                        this.level().setBlockAndUpdate(pos.above(), nox$SMALL_WATER);
                }
            }
        }
    }
}
