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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Endermite.class)
public abstract class EndermiteEntityMixin extends HostileEntityMixin {

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        if(NoxConfig.endermiteMoveSpeedMultiplier > 1) {
            AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null)
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "endermite_bonus"), NoxConfig.endermiteMoveSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public void nox$onSuccessfulAttack(LivingEntity target) {
        if (NoxConfig.endermiteAttacksMakeTargetTeleport && target.level() instanceof ServerLevel serverWorld) {
            double d = target.getX();
            double e = target.getY();
            double f = target.getZ();

            for (int i = 0; i < 16; ++i) {
                double g = target.getX() + (target.getRandom().nextDouble() - 0.5D) * 16.0D;
                double h = Mth.clamp(target.getY() + (double) (target.getRandom().nextInt(16) - 8), serverWorld.getMinBuildHeight(), serverWorld.getMinBuildHeight() + serverWorld.getLogicalHeight() - 1);
                double j = target.getZ() + (target.getRandom().nextDouble() - 0.5D) * 16.0D;

                if (target.isPassenger()) {
                    target.stopRiding();
                }

                if (target.randomTeleport(g, h, j, true)) {
                    serverWorld.playSound(null, d, e, f, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
                    target.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.getMsgId().equals("fall"))
            cir.setReturnValue(!NoxConfig.endermitesImmuneToFallDamage);
        else if (source.getMsgId().equals("inWall"))
            cir.setReturnValue(NoxConfig.endermitesCanSuffocate);
    }


}
