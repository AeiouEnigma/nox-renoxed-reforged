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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void nox$meleeUpdateCheck(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            cir.setReturnValue(false);
        } else if (!livingEntity.isAlive()) {
            if (this.mob.isWithinMeleeAttackRange(livingEntity)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    public void nox$meleeContinueCheck(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            cir.setReturnValue(false);
        } else if (!livingEntity.isAlive()) {
            if (this.mob.isWithinMeleeAttackRange(livingEntity)) {
                cir.setReturnValue(!(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player) livingEntity).isCreative());
            }
        }
    }

}
