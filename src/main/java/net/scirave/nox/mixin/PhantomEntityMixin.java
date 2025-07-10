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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Phantom.class)
public abstract class PhantomEntityMixin extends MobEntityMixin {

    @Override
    public void nox$onTick(CallbackInfo ci) {
        if (NoxConfig.phantomsPhaseThroughBlocks)
            this.noPhysics = true;
    }

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.phantomAttacksApplyWeakness)
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, NoxConfig.phantomWeaknessBiteDuration), (Phantom) (Object) this);
    }

}
