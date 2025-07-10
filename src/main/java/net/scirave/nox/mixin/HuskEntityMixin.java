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

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Husk;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Husk.class)
public abstract class HuskEntityMixin extends ZombieEntityMixin {

    @ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    public MobEffectInstance nox$huskBetterHunger(MobEffectInstance effect) {
        if (NoxConfig.huskAttacksApplyStrongerHunger)
            return new MobEffectInstance(effect.getEffect(), effect.getDuration(), NoxConfig.huskHungerLevel - 1);
        return effect;
    }
}
