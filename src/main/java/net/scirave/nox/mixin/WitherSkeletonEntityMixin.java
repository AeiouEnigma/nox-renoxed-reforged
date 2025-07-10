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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.goals.Nox$MineBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WitherSkeleton.class)
public abstract class WitherSkeletonEntityMixin extends AbstractSkeletonEntityMixin {

    protected WitherSkeletonEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void nox$witherSkeletonArchers(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if (NoxConfig.witherSkeletonArchersExist && this.getRandom().nextBoolean()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
    }

    @ModifyVariable(method = "getArrow", at = @At("HEAD"), argsOnly = true)
    public float nox$witherSkeletonArcherBuff(float original) {
        if (NoxConfig.witherSkeletonArcherDamageMultiplier > 1)
            return original * NoxConfig.witherSkeletonArcherDamageMultiplier;
        return original;
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void nox$witherSkeletonInitGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(4, new Nox$MineBlockGoal((WitherSkeleton) (Object) this));
    }

    @Override
    public void nox$onTick(CallbackInfo ci) {
        if (NoxConfig.witherSkeletonsWitherAuraRadius > 0) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.hasEffect(MobEffects.WITHER) && target.distanceToSqr((WitherSkeleton) (Object) this) <= Mth.square(NoxConfig.witherSkeletonsWitherAuraRadius)) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, NoxConfig.witherSkeletonsWitherAuraDuration), (WitherSkeleton) (Object) this);
            }
        }
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if (NoxConfig.witherSkeletonKnockbackResistanceBonus > 0) {
            AttributeInstance attr = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (attr != null)
                attr.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "wither_skeleton_bonus"), NoxConfig.witherSkeletonKnockbackResistanceBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.witherSkeletonsBreakBlocks;
    }
}
