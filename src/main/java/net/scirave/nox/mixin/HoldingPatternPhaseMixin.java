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

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonHoldingPatternPhase.class)
public abstract class HoldingPatternPhaseMixin extends AbstractDragonPhaseInstance {

    @Shadow
    @Final
    private static TargetingConditions NEW_TARGET_TARGETING;

    public HoldingPatternPhaseMixin(EnderDragon dragon) {
        super(dragon);
    }

    @Shadow
    protected abstract void strafePlayer(Player player);

    @Inject(method = "findNewTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0), cancellable = true)
    public void nox$enderDragonLessStalling(CallbackInfo ci) {
        Player player = this.dragon.level().getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, this.dragon.getX(),this.dragon.getY(),this.dragon.getZ());
        if (player != null) {
            if (this.dragon.getRandom().nextBoolean()) {
                this.strafePlayer(player);
            } else {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());
            }
        }
        ci.cancel();
    }

}
