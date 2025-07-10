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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.Nox$PouncingEntityInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Silverfish.class)
public abstract class SilverfishEntityMixin extends HostileEntityMixin implements Nox$PouncingEntityInterface {

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.silverfishMoveSpeedMultiplier > 1) {
            AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null)
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "silverfish_bonus"), NoxConfig.silverfishMoveSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$silverfishInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(2, new LeapAtTargetGoal((Silverfish) (Object) this, 0.2F));
    }

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.silverfishAttacksGiveMiningFatigue) {
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, NoxConfig.silverfishMiningFatigueBiteDuration, 2), (Silverfish) (Object) this);
        }
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.getMsgId().equals("fall"))
            cir.setReturnValue(NoxConfig.silverfishImmuneToFallDamage);
        else if (source.getMsgId().equals("drown"))
            cir.setReturnValue(NoxConfig.silverfishCanDrown);
        else if (source.getMsgId().equals("inWall"))
            cir.setReturnValue(NoxConfig.silverfishCanSuffocate);
    }

    @Override
    public boolean nox$isAllowedToPounce() {
        return NoxConfig.silverfishPounceAtTarget;
    }

}
