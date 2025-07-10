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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.scirave.nox.util.Nox$MiningInterface;
import net.scirave.nox.util.Nox$PouncingEntityInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
@Mixin(LeapAtTargetGoal.class)
public abstract class PounceAtTargetMixin extends Goal {

    protected long nox$lastPounceUsage = 0;
    @Shadow
    private LivingEntity target;
    @Shadow
    @Final
    private Mob mob;

    @ModifyArgs(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setDeltaMovement(DDD)V"))
    public void nox$betterPounce(Args args) {
        args.set(0, ((double) args.get(0)) * 2);
        args.set(2, ((double) args.get(2)) * 2);
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void nox$betterPounce(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Nox$PouncingEntityInterface && ((Nox$PouncingEntityInterface) mob).nox$isAllowedToPounce()) {
            this.target = this.mob.getTarget();
            if (this.target != null && this.mob.onGround() && !((Nox$MiningInterface) this.mob).nox$isMining()) {
                double d = this.mob.distanceToSqr(this.target);
                if (!(d <= 4.0D) && !(d >= 16.0D)) {
                    if (this.mob.getY() >= (this.target.getY()) - 0.1) {
                        if ((this.mob.level().getGameTime() - this.nox$lastPounceUsage) >= ((Nox$PouncingEntityInterface) mob).nox$pounceCooldown()) {
                            this.nox$lastPounceUsage = this.mob.level().getGameTime();
                            this.mob.getLookControl().setLookAt(this.target);
                            InteractionHand hand = this.mob.getUsedItemHand();
                            if (hand != null) {
                                if (d < 1.0D) {
                                    this.mob.doHurtTarget(target);
                                }
                                this.mob.swing(hand);
                            }
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
        }
        cir.setReturnValue(false);
    }
}
