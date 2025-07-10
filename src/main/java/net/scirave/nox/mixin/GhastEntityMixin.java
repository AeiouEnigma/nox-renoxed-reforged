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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ghast.class)
public abstract class GhastEntityMixin extends MobEntityMixin {

    @Inject(method = "isReflectedFireball", at = @At("HEAD"), cancellable = true)
    private static void nox$ghastNoInstantDeath(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (NoxConfig.ghastFireballsInstantlyKillGhasts)
            cir.setReturnValue(false);
    }

    @Inject(method = "getExplosionPower", at = @At("RETURN"), cancellable = true)
    public void nox$ghastStrongerFireballs(CallbackInfoReturnable<Integer> cir) {
        if(NoxConfig.ghastFireballExplosionStrengthMultiplier > 0){
            cir.setReturnValue(cir.getReturnValue() * NoxConfig.ghastFireballExplosionStrengthMultiplier);
        }
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.ghastBaseHealthMultiplier > 1) {
            AttributeInstance attr = this.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "ghast_bonus"), NoxConfig.ghastBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                this.setHealth(this.getMaxHealth());
            }
        }
    }


}
