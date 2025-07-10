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

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blaze.class)
public abstract class BlazeEntityMixin extends HostileEntityMixin {

    @Override
    public void nox$onTick(CallbackInfo ci) {
        if (NoxConfig.blazeIgnitionAuraRadius > 0) {
            LivingEntity target = this.getTarget();
            if (target != null && target.distanceToSqr((Blaze) (Object) this) <= Mth.square(NoxConfig.blazeIgnitionAuraRadius)) {
                target.igniteForSeconds(NoxConfig.blazeAuraDuration);
            }
        }
    }

}
