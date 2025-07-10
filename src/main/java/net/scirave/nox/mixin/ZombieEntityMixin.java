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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$FleeSunlightGoal;
import net.scirave.nox.goals.Nox$MineBlockGoal;
import net.scirave.nox.util.Nox$PouncingEntityInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class ZombieEntityMixin extends HostileEntityMixin implements Nox$PouncingEntityInterface {

    @Shadow
    protected abstract boolean isSunSensitive();

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if(((Zombie) (Object) this).isBaby() && NoxConfig.babyZombiesGetKnockbackResistance || !((Zombie) (Object) this).isBaby())
        if (NoxConfig.zombieKnockbackResistanceBonus > 0) {
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "zombie_bonus"), NoxConfig.zombieKnockbackResistanceBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (NoxConfig.zombieSpeedMultiplier > 1) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "zombie_bonus"), NoxConfig.zombieSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$zombieInitGoals(CallbackInfo ci) {
        if (this.isSunSensitive()) {
            nox$zombieHideFromSun();
        }

        this.goalSelector.addGoal(0, new Nox$MineBlockGoal((Zombie) (Object) this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal((Zombie) (Object) this, 0.25F));
    }

    public void nox$zombieHideFromSun() {
        this.goalSelector.addGoal(1, new RestrictSunGoal((Zombie) (Object) this));
        this.goalSelector.addGoal(0, new Nox$FleeSunlightGoal((Zombie) (Object) this, 1.0F));
    }

    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.zombiesBreakBlocks;
    }

    @Override
    public boolean nox$isAllowedToPounce() {
        return NoxConfig.zombiesPounceAtTarget;
    }
    @Override
    public int nox$pounceCooldown() {
        return NoxConfig.zombiesPounceCooldown;
    }
}
