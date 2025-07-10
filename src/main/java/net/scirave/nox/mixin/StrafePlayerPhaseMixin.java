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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.pathfinder.Path;
import net.scirave.nox.util.NoxUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class StrafePlayerPhaseMixin extends AbstractDragonPhaseInstance {

    @Shadow
    @Nullable
    private LivingEntity attackTarget;

    @Shadow
    @Nullable
    private Path currentPath;

    private long cooldown = 0;

    private int fireballShots = 0;

    public StrafePlayerPhaseMixin(EnderDragon dragon) {
        super(dragon);
    }

    @Inject(method = "begin", at = @At("HEAD"))
    public void nox$resetDragonFireballs(CallbackInfo ci) {
        this.fireballShots = 0;
    }

    @Inject(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"), cancellable = true)
    public void nox$enderDragonBetterFireball(CallbackInfo ci) {
        if (cooldown > 0 || this.attackTarget == null) {
            cooldown--;
        } else if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0D && this.dragon.hasLineOfSight(this.attackTarget)) {
            cooldown = 20;
            fireballShots++;
            NoxUtil.EnderDragonShootFireball(this.dragon, this.attackTarget);
            if (this.currentPath != null) {
                while (!this.currentPath.isDone()) {
                    this.currentPath.advance();
                }
            }
            if (fireballShots >= 5) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
            }
        }
        ci.cancel();
    }

}
