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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public abstract class HostileEntityMixin extends MobEntityMixin {

    @Shadow public abstract ItemStack getProjectile(ItemStack stack);

    @Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void nox$onSpawnAttempt(EntityType<? extends Monster> type, ServerLevelAccessor world,
                                           MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (type == EntityType.CAVE_SPIDER && spawnReason == MobSpawnType.NATURAL)
            if (pos.getY() >= world.getSeaLevel() || world.canSeeSkyFromBelowWater(pos))
                cir.setReturnValue(false);
    }

}
