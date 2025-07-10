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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.scirave.nox.config.NoxConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(SwellGoal.class)
public abstract class CreeperIgniteGoalMixin extends Goal {

    @Shadow
    @Nullable
    private LivingEntity target;

    @Shadow
    @Final
    private Creeper creeper;

    @Shadow
    public abstract void stop();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void nox$creeperIgniteWhileMoving(Creeper creeper, CallbackInfo ci) {
        EnumSet<Flag> empty = EnumSet.noneOf(Flag.class);
        this.setFlags(empty);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void nox$creeperSmarterIgnite(CallbackInfo ci) {
        double d = this.creeper.distanceToSqr(this.target);
        if (this.target == null) {
            this.creeper.setSwellDir(-1);
        } else if (d > 16.0D) {
            this.creeper.setSwellDir(-1);
        } else if (!NoxConfig.creepersAttackShields && this.target.isBlocking() && this.target.isDamageSourceBlocked(this.creeper.level().damageSources().explosion(this.creeper, this.creeper))) {
            this.creeper.setSwellDir(-1);
        } else {
            this.creeper.setSwellDir(1);
        }
    }

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    public void nox$creeperNoTargetShield(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = this.creeper.getTarget();
        if (!NoxConfig.creepersAttackShields && cir.getReturnValue() && victim != null && victim.isBlocking() && victim.isDamageSourceBlocked(this.creeper.level().damageSources().explosion(this.creeper, this.creeper))) {
            this.creeper.setSwellDir(-1);
            cir.setReturnValue(false);
        }
    }

}
