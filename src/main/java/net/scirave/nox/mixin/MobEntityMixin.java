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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.NoxUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntityMixin {

    @Shadow
    @Final
    protected GoalSelector goalSelector;

    @Shadow
    @Nullable
    public abstract LivingEntity getTarget();

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    public void nox$onAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && target instanceof LivingEntity living) {
            nox$onSuccessfulAttack(living);
        }
    }

    public void nox$onSuccessfulAttack(LivingEntity target) {
        //Overridden
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$initGoals(CallbackInfo ci) {
        //Overridden
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        //Overridden
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", shift = At.Shift.AFTER), method = "<init>")
    public void nox$hostileAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (this instanceof Enemy && this.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "hostile_bonus"), NoxConfig.monsterRangeMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void nox$difficultyScaling(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if (this instanceof Enemy && NoxConfig.monsterGearScales) {
            NoxUtil.weaponRoulette((ServerLevel) this.level(), (Mob) (Object) this, random, localDifficulty);
            NoxUtil.armorRoulette((ServerLevel) this.level(), (Mob) (Object) this, random, localDifficulty);
        }
    }

    @Override
    public void nox$onDamaged(DamageSource source, float amount, CallbackInfo ci) {
        if (this instanceof Enemy && source.getEntity() != null) {
            if (this.isUsingItem()) {
                this.stopUsingItem();
            }
        }
    }

    @Override
    public void nox$onPushAway(Entity entity, CallbackInfo ci) {
        if (this instanceof Enemy && NoxConfig.monsterAngerOnShove && this.getTarget() == null
                && entity instanceof Player player && this.canAttack(player)) {
            nox$maybeAngerOnShove(player);
        }
    }

    public void nox$maybeAngerOnShove(Player player) {
        this.setTarget(player);
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount < this.getHealth() || (this.getHealth()/this.getMaxHealth()) > 0.25) {
            Entity attacker = source.getEntity();
            if (attacker instanceof Mob mob && NoxUtil.isAnAlly(mob, (Mob) (Object) this)) {
                cir.setReturnValue(false);
            }
        }
    }

}
