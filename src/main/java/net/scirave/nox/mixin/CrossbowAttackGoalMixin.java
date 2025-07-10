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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedCrossbowAttackGoal.class)
public class CrossbowAttackGoalMixin {

    @Shadow
    @Final
    private Mob mob;

    @Shadow
    private int attackDelay;
    @Shadow
    private int updatePathDelay;
    @Shadow
    private RangedCrossbowAttackGoal.CrossbowState crossbowState;
    @Shadow
    @Final
    private float attackRadiusSqr;

    private boolean movingLeft = false;
    private int windup = -1;
    private boolean heldShield = false;

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void nox$crossbowDontShootShields(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW);
        if (hand != null && EnchantmentHelper.has(this.mob.getItemInHand(hand), EnchantmentEffectComponents.PROJECTILE_PIERCING))
            return;

        DamageSource fakeSource = mob.level().damageSources().mobProjectile(mob, mob);

        if (windup > -1) {
            if (windup > 0) {
                ci.cancel();
            }
            windup--;
        } else if (target.isBlocking() && target.isDamageSourceBlocked(fakeSource)) {
            heldShield = true;
            ci.cancel();
        } else if (heldShield) {
            heldShield = false;
            windup = 6;
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void nox$crossbowLessDelay(CallbackInfo ci) {
        this.updatePathDelay = 0;
        if (this.attackDelay > 20) {
            this.attackDelay = 20;
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void nox$crossbowStrafe(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (this.crossbowState != RangedCrossbowAttackGoal.CrossbowState.UNCHARGED && target != null) {

            this.mob.lookAt(target, 30.0F, 30.0F);
            boolean backward = false;

            double d = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (d < this.attackRadiusSqr * 0.5D) {
                backward = true;
            }

            if ((double) this.mob.getRandom().nextFloat() < 0.1F) {
                this.movingLeft = !this.movingLeft;
            }

            this.mob.getMoveControl().strafe(backward ? -0.5F : 0.5F, this.movingLeft ? 0.5F : -0.5F);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/valueproviders/UniformInt;sample(Lnet/minecraft/util/RandomSource;)I"))
    public void nox$crossbowPrioritizeCharging(CallbackInfo ci) {
        if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
            this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGING;
            ((CrossbowAttackMob) this.mob).setChargingCrossbow(true);
        }
    }

}
