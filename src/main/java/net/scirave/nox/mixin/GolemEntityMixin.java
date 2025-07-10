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
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGolem.class)
public abstract class GolemEntityMixin extends MobEntityMixin {

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.buffGolems) {
            this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "golem_bonus"), NoxConfig.golemBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            this.setHealth(this.getMaxHealth());
            this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "golem_bonus"), NoxConfig.golemFollowRangeMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
