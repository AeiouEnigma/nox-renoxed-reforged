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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public Level level;
    @Shadow
    public boolean noPhysics;

    @Shadow
    public abstract BlockPos blockPosition();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract RandomSource getRandom();

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void nox$invulnerableCheck(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        // Overridden
    }
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    public void nox$onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci){
        //Overridden
    }
}
