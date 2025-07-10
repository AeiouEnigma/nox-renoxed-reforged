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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin {

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    public void nox$phaseThroughBystanders(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        Entity owner = this.getOwner();

        if (owner instanceof AbstractGolem golemOwner) {
            if ((!(entity instanceof Enemy) && entity != golemOwner.getTarget()) || !golemOwner.canAttackType(entity.getType())) {
                cir.setReturnValue(false);
            }
        }
    }
}
