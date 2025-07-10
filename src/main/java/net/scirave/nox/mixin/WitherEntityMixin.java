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

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.NoxUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnreachableCode")
@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin extends HostileEntityMixin {

    @Shadow
    private int destroyBlocksTick;

    private int nox$reinforcementsCooldown = NoxConfig.witherCallReinforcementsCooldown;

    private void nox$witherBreakBlocks() {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || !NoxConfig.destructiveWither) return;
        AABB box = this.getBoundingBox().inflate(1, 0, 1);

        int i = Mth.floor(box.minX);
        int j = Mth.floor(box.minY);
        int k = Mth.floor(box.minZ);
        int l = Mth.floor(box.maxX);
        int m = Mth.floor(box.maxY);
        int n = Mth.floor(box.maxZ);
        boolean bl = false;

        for (int o = i; o <= l; ++o) {
            for (int p = j; p <= m; ++p) {
                for (int q = k; q <= n; ++q) {
                    BlockPos blockPos = new BlockPos(o, p, q);
                    BlockState blockState = this.level().getBlockState(blockPos);
                    if (!blockState.isAir() && !blockState.is(BlockTags.WITHER_IMMUNE)) {
                        if (NoxUtil.isAtWoodLevel(blockState)) {
                            bl = this.level().removeBlock(blockPos, false) || bl;
                        } else {
                            bl = this.level().destroyBlock(blockPos, true, (WitherBoss) (Object) this) || bl;
                        }
                    }
                }
            }
        }

        if (bl) {
            this.level().levelEvent(null, 1022, this.blockPosition(), 0);
        }

    }

    @Inject(method = "makeInvulnerable", at = @At("TAIL"))
    private void nox$onSummoned(CallbackInfo ci) {
        this.setHealth(this.getMaxHealth());
    }

    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    public void nox$witherNoVanillaBreak(CallbackInfo ci) {
        if (NoxConfig.witherRapidlyBreaksSurroundingBlocks)
            this.destroyBlocksTick = NoxConfig.witherBlockBreakingCooldown;
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    public void nox$witherBetterBreak(CallbackInfo ci) {
        if (NoxConfig.witherRapidlyBreaksSurroundingBlocks)
            nox$witherBreakBlocks();
    }

    @Override
    public void nox$onTick(CallbackInfo ci) {
        LivingEntity target = this.getTarget();
        if (this.level() instanceof ServerLevel serverWorld) {
            if (nox$reinforcementsCooldown > 0) {
                nox$reinforcementsCooldown--;
            } else if (target != null && target.distanceToSqr((WitherBoss) (Object) this) <= Mth.square(NoxConfig.witherReinforcementsTriggerRadius)) {
                nox$reinforcementsCooldown = NoxConfig.witherCallReinforcementsCooldown;
                for (int i = 0; i < NoxConfig.witherReinforcementsGroupSize; i++) {
                    WitherSkeleton skeleton = EntityType.WITHER_SKELETON.create(serverWorld);
                    if (skeleton != null) {
                        skeleton.setPos(this.getX() + this.getRandom().nextIntBetweenInclusive(-2, 2), this.getY(), this.getZ() + this.getRandom().nextIntBetweenInclusive(-2, 2));
                        skeleton.finalizeSpawn(serverWorld, this.level().getCurrentDifficultyAt(skeleton.blockPosition()), MobSpawnType.REINFORCEMENT, null);
                        serverWorld.addFreshEntityWithPassengers(skeleton);
                        skeleton.setTarget(target);
                        skeleton.spawnAnim();
                    }
                }
            }
        }
    }

    @Override
    public void nox$modifyAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        //Non-applicable
    }

    @Override
    public void nox$hostileAttributes(EntityType<?> entityType, Level world, CallbackInfo ci) {
        this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "wither_bonus"), NoxConfig.witherBaseHealthMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        this.setHealth(this.getMaxHealth());
        this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("nox", "wither_bonus"), NoxConfig.witherFollowRangeMultiplier - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public void nox$shouldTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        super.nox$shouldTakeDamage(source, amount, cir);
        if ((source.getMsgId().equals("inWall") && !NoxConfig.withersSuffocate)) {
            cir.setReturnValue(false);
        }
    }

}
