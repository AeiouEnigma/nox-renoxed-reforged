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

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ServerPlayer.class, priority = 100)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {

    // Needed to adjust inject target due to Neo changes.
    @Inject(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;", shift = At.Shift.AFTER), cancellable = true)
    public void nox$sleepNerf(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Vec3 vec3d = Vec3.atBottomCenterOf(pos);
        int seaLevel = ((ServerPlayer) (Object) this).level().getSeaLevel();
        int horizontalSearchDistance = NoxConfig.sleepHorizontalSearchDistance;
        int minVerticalSearchDistance = NoxConfig.sleepMinVerticalSearchDistance;
        boolean extendToSeaLevel = NoxConfig.sleepExtendToSeaLevel;

        double upperY = extendToSeaLevel ? Math.max(vec3d.y() + minVerticalSearchDistance, seaLevel) : vec3d.y() + minVerticalSearchDistance;
        double lowerY = extendToSeaLevel ? Math.min(vec3d.y() - minVerticalSearchDistance, seaLevel) : vec3d.y() - minVerticalSearchDistance;

        List<Monster> list = ((ServerPlayer) (Object) this).level().getEntitiesOfClass(Monster.class, new AABB(
                        vec3d.x() - horizontalSearchDistance, lowerY, vec3d.z() - horizontalSearchDistance,
                        vec3d.x() + horizontalSearchDistance, upperY, vec3d.z() + horizontalSearchDistance),
                (hostileEntity) -> hostileEntity.isPreventingPlayerRest((ServerPlayer) (Object) this)
        );
        if (!list.isEmpty()) {
            if (NoxConfig.sleepApplyGlowing) {
                list.forEach((hostile) -> hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false)));
            }

            cir.setReturnValue(Either.left(Player.BedSleepingProblem.NOT_SAFE));
        }
    }
}
