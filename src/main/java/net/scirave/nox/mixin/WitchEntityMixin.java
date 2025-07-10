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

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Witch.class)
public abstract class WitchEntityMixin extends HostileEntityMixin {

    @Shadow
    public abstract boolean isDrinkingPotion();

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void nox$witchDrinkingFlee(CallbackInfo ci) {
        if (NoxConfig.witchesFleeToDrink) {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>((Witch) (Object) this, LivingEntity.class, 4.0F, 1.1D, 1.35D, (living) -> {
                if (!this.isDrinkingPotion()) return false;

                if (living instanceof Player) {
                    return true;
                } else if (living instanceof Mob mob) {
                    return mob.getTarget() == (Object) this;
                }
                return false;
            }));
        }
    }

    @ModifyArgs(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/RangedAttackGoal;<init>(Lnet/minecraft/world/entity/monster/RangedAttackMob;DIF)V"))
    public void nox$witchFasterAttack(Args args) {
        args.set(2, Mth.ceil((int) args.get(2) * 0.75));
    }

    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    public Holder<Potion> nox$witchUpgradedPotions(Holder<Potion> original) {
        if (NoxConfig.witchesDrinkBetterPotions) {
            if (Potions.WATER_BREATHING.equals(original)) {
                return Potions.LONG_WATER_BREATHING;
            } else if (Potions.FIRE_RESISTANCE.equals(original)) {
                return Potions.LONG_FIRE_RESISTANCE;
            } else if (Potions.HEALING.equals(original)) {
                return Potions.STRONG_HEALING;
            } else if (Potions.SWIFTNESS.equals(original)) {
                return Potions.STRONG_SWIFTNESS;
            }
        }
        return original;
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    public void nox$witchNoDrinkingSlowdown(AttributeInstance instance, AttributeModifier modifier) {
        // No slowdown!
    }

    @ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    public Item nox$witchLingeringPotions(Item original) {
        if (NoxConfig.witchesUseLingeringPotions)
            return Items.LINGERING_POTION;
        return original;
    }

    @ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    public Holder<Potion> nox$witchUpgradedSlowness(Holder<Potion> original) {
        if (NoxConfig.witchesUseStrongerSlowness && Potions.SLOWNESS.equals(original)) {
            return Potions.STRONG_SLOWNESS;
        }
        return original;
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.equals(this.level().damageSources().magic()))
            cir.setReturnValue(NoxConfig.witchesTakeMagicDamage);
        if (source.typeHolder().is(DamageTypeTags.IS_PROJECTILE) && !source.typeHolder().is(DamageTypeTags.BYPASSES_ARMOR))
            cir.setReturnValue(!NoxConfig.witchesResistProjectiles);
    }

    @Redirect(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isDrinkingPotion()Z"))
    public boolean nox$witchDrinkWhileAttack(Witch instance) {
        return false;
    }

    @ModifyArgs(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;shoot(DDDFF)V"))
    public void nox$witchBetterAim(Args args) {
        args.set(1, (double) args.get(1) * 0.50);
        args.set(3, (float) ((float) args.get(3) + 0.25));
        args.set(4, (float) args.get(4) / 4);
    }

}
