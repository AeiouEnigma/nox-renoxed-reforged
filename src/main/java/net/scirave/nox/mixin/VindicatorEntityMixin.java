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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$MineBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vindicator.class)
public abstract class VindicatorEntityMixin extends HostileEntityMixin {

    @Override
    public void nox$initGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Nox$MineBlockGoal((Vindicator) (Object) this));
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.vindicatorKnockbackResistanceBonus > 0) {
            AttributeInstance attr = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (attr != null)
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "vindicator_bonus"), NoxConfig.vindicatorKnockbackResistanceBonus, AttributeModifier.Operation.ADD_VALUE));
        }
        if (NoxConfig.vindicatorSpeedBonus > 1) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "vindicator_bonus"), NoxConfig.vindicatorSpeedBonus - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.vindicatorsBreakBlocks;
    }

}
