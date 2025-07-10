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
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonChargePlayerPhase.class)
public abstract class ChargingPlayerPhaseMixin extends AbstractDragonPhaseInstance {

    private static final TargetingConditions RANGE_PREDICATE = TargetingConditions.forCombat().ignoreLineOfSight();

    @Shadow
    @Nullable
    private Vec3 targetLocation;

    @Shadow
    private int timeSinceCharge;

    public ChargingPlayerPhaseMixin(EnderDragon dragon) {
        super(dragon);
    }

    @Shadow
    public abstract void setTarget(Vec3 pathTarget);

    @Inject(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V", ordinal = 1), cancellable = true)
    public void nox$enderDragonLongerCharging(CallbackInfo ci) {
        if (this.targetLocation != null && this.timeSinceCharge++ < 200) {
            double d = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            if (d < 100.0D || d > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
                ++this.timeSinceCharge;
            }
        } else {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
        }
        ci.cancel();
    }

    @Inject(method = "doServerTick", at = @At(value = "HEAD"))
    public void nox$enderDragonBetterCharging(CallbackInfo ci) {
        Player player = this.dragon.level().getNearestPlayer(RANGE_PREDICATE, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (player != null) {
            this.setTarget(player.position());

        }
    }

}
