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

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.scirave.nox.util.Nox$MiningInterface;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements Nox$MiningInterface {

    @Shadow
    public abstract Brain<?> getBrain();

    @Unique
    private boolean nox$mining = false;
    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean hasLineOfSight(Entity entity);

    @Shadow
    public abstract boolean canAttack(LivingEntity target);

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public abstract void stopUsingItem();

    @Shadow public abstract float getHealth();

    @Shadow @Nullable public abstract AttributeInstance getAttribute(Holder<Attribute> attribute);

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    public void nox$ghastFireballsPierce(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (
                (source.getEntity() instanceof Ghast) ||
                        (source.getEntity() instanceof Phantom)
        ) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void nox$onStatusEffect(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        //Overridden
    }

    @Inject(method = "die", at = @At("HEAD"))
    public void nox$onDeath(DamageSource source, CallbackInfo ci) {
        //Overridden
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        //Overridden
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    public void nox$onDamaged(DamageSource source, float amount, CallbackInfo ci) {
        //Overridden
    }

    @Inject(method = "doPush", at = @At("HEAD"))
    public void nox$onPushAway(Entity entity, CallbackInfo ci) {
        //Overridden
    }


    @Inject(method = "tick", at = @At("HEAD"))
    public void nox$onTick(CallbackInfo ci) {
        //Overridden
    }

    @Override
    public boolean nox$isMining() {
        return this.nox$mining;
    }

    @Override
    public void nox$setMining(boolean bool) {
        this.nox$mining = bool;
    }

}
