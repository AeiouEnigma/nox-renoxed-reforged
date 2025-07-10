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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Evoker.class)
public abstract class EvokerEntityMixin extends HostileEntityMixin {

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.evokerBaseHealthMultiplier > 1) {
            AttributeInstance attr = this.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "evoker_bonus"), NoxConfig.evokerBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                this.setHealth(this.getMaxHealth());
            }
        }
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.equals(this.level().damageSources().magic()))
            cir.setReturnValue(!NoxConfig.evokersImmuneToMagic);
        if (source.typeHolder().is(DamageTypeTags.IS_PROJECTILE) && !source.typeHolder().is(DamageTypeTags.BYPASSES_ARMOR))
            cir.setReturnValue(!NoxConfig.evokersResistProjectiles);
    }

}
