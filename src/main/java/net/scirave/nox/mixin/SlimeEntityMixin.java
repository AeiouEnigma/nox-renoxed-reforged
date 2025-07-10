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

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Slime.class)
public abstract class SlimeEntityMixin extends MobEntityMixin {

    @Shadow
    public abstract int getSize();

    @Shadow
    public abstract void setSize(int size, boolean heal);

    @Shadow
    public abstract EntityType<? extends Slime> getType();

    @Inject(method = "checkSlimeSpawnRules", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;seedSlimeChunk(IIJJ)Lnet/minecraft/util/RandomSource;"), cancellable = true)
    private static void nox$slimeSpawnNaturally(EntityType<Slime> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (NoxConfig.slimeNaturalSpawn) {
            if (world.getBrightness(LightLayer.BLOCK, pos) <= NoxConfig.slimeNaturalSpawnMaxBlockLight
                    && world.getBrightness(LightLayer.SKY, pos) - world.getSkyDarken() <= NoxConfig.slimeNaturalSpawnMaxSkyLight) {
                cir.setReturnValue(Slime.checkMobSpawnRules(type, world, spawnReason, pos, random));
            }
        }
    }

    @Inject(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    public void nox$betterSlimeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData entityData, CallbackInfoReturnable<MobSpawnSettings> cir) {
        int size = 3;
        float random = this.getRandom().nextFloat() * 10 + difficulty.getEffectiveDifficulty();

        if (random < 5) {
            size = 2;
        }
        if (random < 2) {
            size = 1;
        }
        if (random > 9.5) {
            size = 4;
        }

        this.setSize(size, true);
    }

    @Inject(method = "getJumpDelay", at = @At("HEAD"), cancellable = true)
    private void nox$makeSlimesJumpConstantly(CallbackInfoReturnable<Integer> cir) {
        if (NoxConfig.slimesJumpConstantly)
            cir.setReturnValue(4);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;setSize(IZ)V"))
    public void nox$slimeReapplyAttributes(Entity.RemovalReason reason, CallbackInfo ci, @Local(ordinal = 1) Slime slimeEntity) {
        if (this.level() instanceof ServerLevel serverWorld) {
            slimeEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.REINFORCEMENT, null);
        }
    }

    @Inject(method = "dealDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffects(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void nox$slimeOnAttack(LivingEntity victim, CallbackInfo ci) {
        //Overridden
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        AttributeInstance attr;
        if (NoxConfig.slimeBaseHealthMultiplier > 1) {
            attr = this.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "slime_bonus"), NoxConfig.slimeBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                this.setHealth(this.getMaxHealth());
            }
        }
        if (NoxConfig.slimeFollowRangeMultiplier > 1) {
            attr = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (attr != null)
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "slime_bonus"), NoxConfig.slimeFollowRangeMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (NoxConfig.slimeMoveSpeedMultiplier > 1) {
            attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null)
                attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "slime_bonus"), NoxConfig.slimeMoveSpeedMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        attr = this.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (attr != null)
            attr.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "slime_bonus"), 0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if (source.getMsgId().equals("fall"))
            cir.setReturnValue(!NoxConfig.slimesImmuneToFallDamage);
        else if (source.typeHolder().is(DamageTypeTags.IS_PROJECTILE) && !source.typeHolder().is(DamageTypeTags.BYPASSES_ARMOR))
            cir.setReturnValue(!NoxConfig.slimesResistProjectiles);
    }

    @Override
    public void nox$onDeath(DamageSource source, CallbackInfo ci) {
        if (this.level() instanceof ServerLevel) {
            this.nox$slimeOnDeath();
        }
    }

    public void nox$slimeOnDeath() {
        if (NoxConfig.slimePoisonCloudOnDeath && NoxConfig.slimePoisonCloudDurationDivisor > 0 && this.getType() == EntityType.SLIME) {
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            cloud.setRadius(NoxConfig.slimePoisonCloudRadiusMultiplier * this.getSize());
            cloud.setRadiusOnUse(-0.5F);
            cloud.setWaitTime(10 + 15 * (this.getSize() - 1));
            cloud.setDuration(cloud.getDuration() * this.getSize() / NoxConfig.slimePoisonCloudDurationDivisor);
            cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, NoxConfig.slimePoisonDuration, NoxConfig.slimePoisonAmplifier - 1));
            this.level().addFreshEntity(cloud);
        }
    }

    @Override
    public void nox$onStatusEffect(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (effect.getEffect() == MobEffects.POISON) {
            cir.setReturnValue(false);
        }
    }

}
