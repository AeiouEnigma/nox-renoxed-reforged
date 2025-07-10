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

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.scirave.nox.Nox;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CaveSpider.class)
public abstract class CaveSpiderEntityMixin extends SpiderEntityMixin {

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.caveSpiderAttacksPlaceWebs) {
            BlockPos pos = target.blockPosition();
            if (this.level().getBlockState(pos).canBeReplaced())
                this.level().setBlockAndUpdate(pos, Nox.NOX_COBWEB.defaultBlockState());
        }
        if (NoxConfig.caveSpidersApplySlowness)
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, NoxConfig.caveSpiderSlownessBiteDuration, NoxConfig.caveSpiderSlownessBiteLevel - 1), (CaveSpider) (Object) this);
    }

}
