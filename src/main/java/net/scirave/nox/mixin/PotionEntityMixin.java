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

import net.minecraft.world.entity.projectile.ThrownPotion;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownPotion.class)
public abstract class PotionEntityMixin extends ProjectileEntityMixin {

    @ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadius(F)V"))
    public float nox$witchBiggerPotionRadius(float original) {
        if(NoxConfig.witchesUseLingeringPotions){
            return original * NoxConfig.witchLingeringPotionRadiusMultiplier;
        }
        return original;
    }

    @ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;setWaitTime(I)V"))
    public int nox$witchFasterCloudWindup(int original) {
        return original / NoxConfig.witchPotionWindupDivisor;
    }

}
