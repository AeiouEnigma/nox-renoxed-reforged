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

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public abstract class DragonFireballEntityMixin extends AbstractHurtingProjectile {

    protected DragonFireballEntityMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    public void nox$enderDragonLessFireballCollision(HitResult hitResult, CallbackInfo ci) {
        if (hitResult.getType() == HitResult.Type.ENTITY && (((EntityHitResult) hitResult).getEntity() instanceof EnderDragonPart)) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public Entity nox$enderDragonAdjustedFireball(Entity entity) {
        if (NoxConfig.buffEnderDragonFireball && this.getOwner() instanceof EnderDragon && entity instanceof AreaEffectCloud cloud) {
            cloud.setDuration(NoxConfig.enderDragonBreathDuration);
            cloud.setRadius(NoxConfig.enderDragonBreathRadius);
            cloud.setWaitTime(cloud.getWaitTime() / 2);
            cloud.setRadiusOnUse(0);
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 2));
        }
        return entity;
    }

}
