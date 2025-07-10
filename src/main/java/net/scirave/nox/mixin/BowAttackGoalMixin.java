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

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class BowAttackGoalMixin<T extends Mob & RangedAttackMob> {

    @Shadow
    @Final
    private T mob;

    @Shadow
    private boolean strafingClockwise;
    @Shadow
    private boolean strafingBackwards;
    @Shadow
    @Final
    private float attackRadiusSqr;
    private int windup = -1;

    private boolean heldShield = false;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getTicksUsingItem()I"), cancellable = true)
    public void nox$bowDontShootShields(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        DamageSource fakeSource = mob.level().damageSources().mobProjectile(mob, mob);

        if (windup > -1) {
            if (windup > 0) {
                ci.cancel();
            }
            windup--;
        } else if (target.isBlocking() && target.isDamageSourceBlocked(fakeSource)) {
            heldShield = true;
            ci.cancel();
        } else if (heldShield) {
            heldShield = false;
            windup = 6;
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void nox$newBowStrafe(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {

            this.strafingBackwards = false;

            double d = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (d < (this.attackRadiusSqr * 0.25D)) {
                this.strafingBackwards = true;
            }

            if ((double) this.mob.getRandom().nextFloat() < 0.1F) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            this.mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
        }
    }

}
