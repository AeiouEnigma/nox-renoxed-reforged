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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Stray.class)
public abstract class StrayEntityMixin extends AbstractSkeletonEntityMixin {

    protected StrayEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyArg(method = "getArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Arrow;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    public MobEffectInstance nox$strayBetterSlowness(MobEffectInstance effect) {
        if (NoxConfig.strayAttacksApplyStrongerSlowness)
            return new MobEffectInstance(effect.getEffect(), effect.getDuration(), NoxConfig.straySlownessLevel - 1);
        return effect;
    }

}
