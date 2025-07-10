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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$FleeSunlightGoal;
import net.scirave.nox.util.Nox$SwimGoalInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonEntityMixin extends Monster implements Nox$SwimGoalInterface, RangedAttackMob {

    @Unique
    private Vec3 nox$targetVelocity = Vec3.ZERO;
    @Unique
    private Vec3 nox$lastTargetVelocity = Vec3.ZERO;
    @Unique
    private Vec3 nox$velocityDifference = Vec3.ZERO;

    protected AbstractSkeletonEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$skeletonInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new Nox$FleeSunlightGoal((AbstractSkeleton) (Object) this, 1.0F));
        this.goalSelector.addGoal(1, new FloatGoal((AbstractSkeleton) (Object) this));
    }

    @ModifyExpressionValue(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
    private double addXArrowVelocity(double original, @Local(argsOnly = true) LivingEntity entity) {
        return NoxConfig.skeletonImprovedAim ? original + this.nox$targetVelocity.x * 10 : original;
    }

    @ModifyExpressionValue(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
    private double addZArrowVelocity(double original, @Local(argsOnly = true) LivingEntity entity) {
        return NoxConfig.skeletonImprovedAim ? original + this.nox$targetVelocity.z * 10 : original;
    }

    @ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shoot(DDDFF)V"), index = 3)
    private float changeArrowPower(float original) {
        return NoxConfig.skeletonShootArrowPower;
    }


    @Inject(method = "aiStep", at = @At("HEAD"))
    public void nox$onTick(CallbackInfo ci) {
        if (NoxConfig.skeletonImprovedAim && this.getTarget() != null) {
            this.nox$velocityDifference = this.nox$velocityDifference.scale(3).add(this.getTarget().getDeltaMovement().subtract(this.nox$lastTargetVelocity)).scale(0.25);
            this.nox$targetVelocity = this.nox$targetVelocity.scale(3).add(this.getTarget().getDeltaMovement().add(this.nox$velocityDifference.scale(5))).scale(0.25);
            this.nox$lastTargetVelocity = this.getTarget().getDeltaMovement();
        }
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.skeletonSpeedMultiplier > 1) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "generic_skeleton_bonus"), NoxConfig.skeletonSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public boolean nox$canSwim() {
        return NoxConfig.skeletonsCanSwim;
    }


    public boolean nox$isAllowedToMine() {
        return false;
    }
}
