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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedAttackGoal.class)
public abstract class ProjectileAttackGoalMixin {

    @Shadow
    @Nullable
    private LivingEntity target;

    @Shadow
    @Final
    private Mob mob;

    @Shadow
    @Final
    private float attackRadius;
    @Shadow
    private int attackTime;
    private boolean movingLeft = false;

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void nox$projectileStrafe(CallbackInfo ci) {
        if (this.target != null) {
            this.mob.lookAt(target, 30.0F, 30.0F);
            boolean backward = false;

            double d = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (d < (this.attackRadius * this.attackRadius * 0.75)) {
                backward = true;
            }

            if ((double) this.mob.getRandom().nextFloat() < 0.1F) {
                this.movingLeft = !this.movingLeft;
            }

            this.mob.getMoveControl().strafe(backward ? -0.5F : 0.5F, this.movingLeft ? 0.5F : -0.5F);
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void nox$projectileMaybeDontShootShields(CallbackInfo ci) {
        if (this.nox$projectileShouldntShootShields()) {
            this.attackTime++;
            ci.cancel();
        }
    }

    public boolean nox$projectileShouldntShootShields() {
        return false;
    }

}
