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
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Guardian;
import net.scirave.nox.config.NoxConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class ActiveTargetGoalMixin extends TargetGoal {

    @Shadow
    protected TargetingConditions targetConditions;

    @Shadow
    @Nullable
    protected LivingEntity target;

    public ActiveTargetGoalMixin(Mob mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @Shadow
    protected abstract void findTarget();

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("TAIL"))
    public void nox$seeThroughWalls(Mob mob, Class targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, Predicate targetPredicate, CallbackInfo ci) {
        if (NoxConfig.mobXray && (mob instanceof Enemy || mob instanceof NeutralMob || mob instanceof AbstractGolem)) {
            this.targetConditions.ignoreLineOfSight();
        }
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void nox$noRandomTarget(CallbackInfoReturnable<Boolean> cir) {
        this.findTarget();
        if (this.mob instanceof Guardian && NoxConfig.guardianConstantBeam) {
            return;
        }
        cir.setReturnValue(this.target != null);
    }
}
