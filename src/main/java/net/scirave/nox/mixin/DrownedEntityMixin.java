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

import net.minecraft.world.entity.monster.Drowned;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Drowned.class)
public abstract class DrownedEntityMixin extends ZombieEntityMixin {

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Drowned;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"))
    private float nox$drownedFasterSwimming(float original) {
        if (NoxConfig.drownedSwimSpeedMultiplier > 1)
            return original * NoxConfig.drownedSwimSpeedMultiplier;
        return original;
    }

    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.drownedBreakBlocks;
    }
}
