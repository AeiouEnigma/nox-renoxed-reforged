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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChorusFlowerBlock.class)
public class ChorusFlowerBlockMixin extends AbstractBlockMixin {

    @Override
    public void nox$onBlockReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo c) {
        if (!moved && world instanceof ServerLevel serverWorld && NoxConfig.endermiteFlowerSpawn && world.random.nextIntBetweenInclusive(1, 3) == 3) {
            Endermite endermite = EntityType.ENDERMITE.create(world);
            if (endermite != null) {
                endermite.absMoveTo((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5, 0.0F, 0.0F);
                world.addFreshEntity(endermite);
                endermite.spawnAnim();

                endermite.finalizeSpawn(serverWorld, world.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null);
            }
        }
    }
}
