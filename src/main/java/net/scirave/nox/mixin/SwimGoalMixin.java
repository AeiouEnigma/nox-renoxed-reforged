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

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.scirave.nox.util.Nox$SwimGoalInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FloatGoal.class)
public abstract class SwimGoalMixin {

    @Final
    @Shadow
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void nox$maybeAllowSwimming(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Nox$SwimGoalInterface && !((Nox$SwimGoalInterface) mob).nox$canSwim())
            cir.setReturnValue(false);
    }

}
