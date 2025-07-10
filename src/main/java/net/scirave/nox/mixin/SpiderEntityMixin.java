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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.monster.Spider;
import net.scirave.nox.Nox;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Spider.class)
public abstract class SpiderEntityMixin extends HostileEntityMixin {

    @Inject(method = "registerGoals", at = @At("HEAD"))
    public void nox$spiderInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new RestrictSunGoal((Spider) (Object) this));
    }

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.spiderAttacksPlaceWebs && this.getType().getWidth() >= EntityType.CAVE_SPIDER.getWidth()) {
            BlockPos pos = target.blockPosition();
            if (this.level().getBlockState(pos).canBeReplaced())
                this.level().setBlockAndUpdate(pos, Nox.NOX_COBWEB.defaultBlockState());
        }
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.getMsgId().equals("fall")) {
            cir.setReturnValue(!NoxConfig.spidersImmuneToFallDamage);
        }
    }

}
