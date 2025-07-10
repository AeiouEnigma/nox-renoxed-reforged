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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$MineBlockGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderMan.class)
public abstract class EndermanEntityMixin extends HostileEntityMixin {

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    @Shadow
    protected abstract boolean teleport();

    @Shadow
    public abstract void setBeingStaredAt();

    @Inject(method = "setTarget", at = @At("HEAD"))
    public void nox$endermanBlindOnProvoked(LivingEntity target, CallbackInfo ci) {
        if (NoxConfig.endermanAppliesBlindnessOnAggro && this.getTarget() != target && target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, NoxConfig.endermanBlindnessStareDuration), (EnderMan) (Object) this);
        }
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void nox$endermanLessRandomTeleport(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, boolean entity) {
        if (source.equals(source.type().equals(DamageTypes.ON_FIRE) || source.type().equals(DamageTypes.MAGIC))){
            cir.setReturnValue(entity);
        }
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"))
    public void nox$endermanTeleportOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.isAlive() && NoxConfig.endermanTeleportsFromMeleeHit && source.getEntity() instanceof LivingEntity && !source.type().equals(DamageTypes.ON_FIRE) && !source.type().equals(DamageTypes.MAGIC)) {
            for (int i = 0; i < 64; ++i) {
                if (this.teleport()) {
                    break;
                }
            }
        }
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$endermanInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Nox$MineBlockGoal((EnderMan) (Object) this));
    }

    @Override
    public void nox$maybeAngerOnShove(Player player) {
        super.nox$maybeAngerOnShove(player);
        this.setBeingStaredAt();
    }

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.endermanAppliesBlindnessOnHit)
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, NoxConfig.endermanBlindnessHitDuration), (EnderMan) (Object) this);
    }

    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.endermenBreakBlocks;
    }
}
