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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.NoxUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.logging.Level;

@Mixin(EnderDragon.class)
public abstract class EnderDragonEntityMixin extends MobEntityMixin {

    private static final TargetingConditions nox$RANGE_PREDICATE = TargetingConditions.forCombat();
    private int nox$fireballCooldown = 0;

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.typeHolder().is(DamageTypeTags.IS_EXPLOSION)) {
            cir.setReturnValue(!NoxConfig.enderDragonIsImmuneToExplosionDamage);
        }
    }

    @Override
    public void nox$onTick(CallbackInfo ci) {
        if (nox$fireballCooldown <= 0) {
            Player player = this.level().getNearestPlayer(nox$RANGE_PREDICATE, (EnderDragon) (Object) this, ((EnderDragon) (Object) this).getX(), ((EnderDragon) (Object) this).getY(), ((EnderDragon) (Object) this).getZ());
            if (player != null && player.distanceToSqr((EnderDragon) (Object) this) >= 49.0D && this.hasLineOfSight(player)) {
                nox$fireballCooldown = NoxConfig.enderDragonFireballCooldown;
                NoxUtil.EnderDragonShootFireball((EnderDragon) (Object) this, player);
            }
        } else {
            nox$fireballCooldown--;
        }

    }
    //@Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        //Non-applicable
    }
    //@Override
    public void nox$hostileAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "ender_dragon_bonus"), NoxConfig.enderDragonBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        this.setHealth(this.getMaxHealth());
    }

}
