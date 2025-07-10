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

import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$MineBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractPiglin.class)
public abstract class AbstractPiglinEntityMixin extends HostileEntityMixin {

    @Override
    public void nox$initGoals(CallbackInfo ci) {
        if (this.nox$isAllowedToMine()) {
            this.goalSelector.addGoal(1, new Nox$MineBlockGoal((AbstractPiglin) (Object) this));
        }
    }
    
    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.piglinsBreakBlocks;
    }

}
