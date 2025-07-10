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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$CreeperBreachGoal;
import net.scirave.nox.util.Nox$CreeperBreachInterface;
import net.scirave.nox.util.Nox$PouncingEntityInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.scirave.nox.config.NoxConfig.creepersExplodeOnDeath;


@Mixin(Creeper.class)
public abstract class CreeperEntityMixin extends HostileEntityMixin implements Nox$CreeperBreachInterface, Nox$PouncingEntityInterface {

    @Shadow protected abstract void explodeCreeper();

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void nox$creeperInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(2, new LeapAtTargetGoal((Creeper) (Object) this, 0.3F));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>((Creeper) (Object) this, LivingEntity.class,
                4.0F, 1.5D, 1.7D, (living) -> {
            if (!NoxConfig.creepersRunFromShields) return false;
            if (living instanceof LivingEntity livingEntity) {
                return livingEntity.isBlocking() && livingEntity.isDamageSourceBlocked(this.level().damageSources().explosion((Creeper) (Object) this, (Creeper) (Object) this));
            }
            return false;
        }));
        if (NoxConfig.creeperBreachDistance > 0) {
            this.goalSelector.addGoal(3, new Nox$CreeperBreachGoal((Creeper) (Object) this));
        }
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.creeperSpeedMultiplier > 1) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "creeper_bonus"), NoxConfig.creeperSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public void nox$onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci) {
        if (creepersExplodeOnDeath) {
            if (reason == Entity.RemovalReason.KILLED) {
                ci.cancel();
                ((Creeper) (Object) this).remove(Entity.RemovalReason.DISCARDED);
                explodeCreeper();
            }
        }
    }

    @Override
    public boolean nox$isAllowedToBreachWalls() {
        return NoxConfig.creepersBreachWalls;
    }

    @Override
    public boolean nox$isAllowedToPounce() {
        return NoxConfig.creepersPounceAtTarget;
    }

    @Override
    public int nox$pounceCooldown() {
        return NoxConfig.creepersPounceCooldown;
    }
}