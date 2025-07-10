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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(IronGolem.class)
public abstract class IronGolemEntityMixin extends GolemEntityMixin {

    @Shadow
    public abstract boolean canAttackType(EntityType<?> type);

    @Shadow
    public abstract boolean doHurtTarget(Entity target);

    private boolean nox$canSweepAttack = true;

    @Inject(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffects(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void nox$ironGolemSweepAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (NoxConfig.ironGolemsHaveASweepAttack) {
            if (this.nox$canSweepAttack) {
                this.nox$canSweepAttack = false;
                List<Mob> list = this.level().getEntitiesOfClass(Mob.class, AABB.ofSize(target.position(), 1, 1, 1), (mob) -> (mob instanceof Enemy || mob.getTarget() == (Object) this) && this.canAttackType(mob.getType()) && this.canAttackType(mob.getType()));
                for (Mob mob : list) {
                    this.doHurtTarget(mob);
                }
            }
            this.nox$canSweepAttack = true;
        }
    }

}
