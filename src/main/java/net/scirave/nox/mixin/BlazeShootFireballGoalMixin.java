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
import net.minecraft.world.entity.monster.Blaze;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blaze.BlazeAttackGoal.class)
public abstract class BlazeShootFireballGoalMixin {

    @Shadow
    private int attackTime;

    @Shadow
    @Final
    private Blaze blaze;
    @Shadow
    private int attackStep;
    private int windup = -1;
    private boolean movingLeft = false;
    private boolean heldShield = false;

    @Shadow
    protected abstract double getFollowDistance();

    boolean extraTick = false;
    @Inject(method = "tick", at = @At("HEAD"))
    public void nox$blazeLessFireballCooldown(CallbackInfo ci) {
        if(NoxConfig.lessBlazeFireballCooldown) {
            if (extraTick) {
                this.attackTime--;
                extraTick = false;
            } else {
                extraTick = true;
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Blaze;getX()D", ordinal = 0), cancellable = true)
    public void nox$blazeDontShootShields(CallbackInfo ci) {
        if (this.attackStep == 1) {
            LivingEntity target = this.blaze.getTarget();
            if (target == null) return;

            DamageSource fakeSource = this.blaze.level().damageSources().mobProjectile(this.blaze, this.blaze);

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
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void nox$blazeStrafe(CallbackInfo ci) {
        LivingEntity target = this.blaze.getTarget();
        if (target != null) {

            this.blaze.lookAt(target, 30.0F, 30.0F);
            boolean backward = false;

            double d = this.blaze.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (d < this.getFollowDistance() * 0.75) {
                backward = true;
            }

            if ((double) this.blaze.getRandom().nextFloat() < 0.1F) {
                this.movingLeft = !this.movingLeft;
            }

            this.blaze.getMoveControl().strafe(backward ? -0.5F : 0.5F, this.movingLeft ? 0.5F : -0.5F);
        }
    }

}
