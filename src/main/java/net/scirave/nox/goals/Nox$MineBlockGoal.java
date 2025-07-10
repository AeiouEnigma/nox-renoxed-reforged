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

package net.scirave.nox.goals;


import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.Nox$MiningInterface;
import net.scirave.nox.util.NoxUtil;
import org.jetbrains.annotations.Nullable;

import static net.scirave.nox.util.NoxUtil.NOX_ALWAYS_MINE;
import static net.scirave.nox.util.NoxUtil.NOX_CANT_MINE;

public class Nox$MineBlockGoal extends Goal {

    protected final Monster owner;
    private LivingEntity target;

    @Nullable
    private LivingEntity targetSeen;
    private BlockPos posToMine;
    private int mineTick;
    @Nullable
    private GameProfile profile;

    public Nox$MineBlockGoal(Monster living) {
        this.owner = living;
    }

    public static float getAdjustedHardness(Level world, BlockPos pos) {
        float hardness = world.getBlockState(pos).getDestroySpeed(world, pos);
        if (hardness < 0) {
            return 0.0F;
        }
        return 2.0F / hardness / 30F;
    }

    public static boolean canMine(BlockState block) {
        if (block.is(NOX_ALWAYS_MINE)) {
            return NoxUtil.isAtWoodLevel(block);
        } else if (block.getBlock().defaultDestroyTime() >= NoxConfig.blockBreakingHardnessCutoff || block.is(NOX_CANT_MINE)) {
            return false;
        } else {
            return NoxUtil.isAtWoodLevel(block);
        }
    }

    public @Nullable BlockPos findBlock(LivingEntity victim, @Nullable Path path) {
        BlockPos origin = this.owner.blockPosition();

        if (path != null && path.getEndNode() != null) {
            origin = path.getEndNode().asBlockPos();
        }

        int yMod = 1;
        BlockPos elected;

        if (isBreakable(this.owner.level(), origin.above())) {
            return origin.above();
        }

        int xDiff = victim.getBlockX() - this.owner.getBlockX();
        int zDiff = victim.getBlockZ() - this.owner.getBlockZ();

        int absXDiff = Math.abs(xDiff);
        int absZDiff = Math.abs(zDiff);

        if (this.owner.getBlockY() > victim.getBlockY()) {
            yMod = 0;
            if (isBreakable(this.owner.level(), origin.below())) {
                return origin.below();
            }
            if (absXDiff == absZDiff) {
                elected = searchForBlock(this.owner.level(), origin);
                if (elected != null) {
                    return elected;
                }
            }
        } else if (this.owner.getBlockY() < victim.getBlockY()) {
            yMod = 2;
            if (isBreakable(this.owner.level(), origin.above(2))) {
                return origin.above(2);
            }
            if (absXDiff == absZDiff) {
                elected = searchForBlock(this.owner.level(), origin.above());
                if (elected != null) {
                    return elected;
                }
            }
        }

        if (isBreakable(this.owner.level(), origin.above().above(yMod - 1))) {
            return origin.above();
        }

        if (absXDiff > absZDiff) {
            if (xDiff > 0) {
                elected = searchForBlock(this.owner.level(), origin.east().above(yMod));
            } else {
                elected = searchForBlock(this.owner.level(), origin.west().above(yMod));
            }
            if (elected != null) {
                return elected;
            }
            if (zDiff > 0) {
                elected = searchForBlock(this.owner.level(), origin.south().above(yMod));
            } else {
                elected = searchForBlock(this.owner.level(), origin.north().above(yMod));
            }
        } else {
            if (zDiff > 0) {
                elected = searchForBlock(this.owner.level(), origin.south().above(yMod));
            } else {
                elected = searchForBlock(this.owner.level(), origin.north().above(yMod));
            }
            if (elected != null) {
                return elected;
            }
            if (xDiff > 0) {
                elected = searchForBlock(this.owner.level(), origin.east().above(yMod));
            } else {
                elected = searchForBlock(this.owner.level(), origin.west().above(yMod));
            }
        }

        return elected;
    }

    @Override
    public boolean canUse() {
        if (!((Nox$MiningInterface) owner).nox$isAllowedToMine())
            return false;
        if (canContinueTouse()) {
            return true;
        } else if (!NoxConfig.mobsBreakBlocks) {
            return false;
        }
        LivingEntity victim = this.owner.getTarget();
        if (victim == null || victim.isDeadOrDying()) {
            this.targetSeen = null;
            return false;
        }

        if (this.owner.tickCount > 60 && (this.owner.onGround() || this.owner.isInWater()) && victim.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {

            if (this.targetSeen != victim) {
                if (this.owner.canAttack(victim)) {
                    this.targetSeen = victim;
                } else {
                    return false;
                }
            }

            Path path = this.owner.getNavigation().createPath(victim, 0);
            if (path != null && path.canReach()) {
                return false;
            }
            if (path == null || path.getEndNode() == null || path.getEndNode().distanceToSqr(this.owner.blockPosition()) <= 1) {
                BlockPos blockPos = this.findBlock(victim, path);
                if (blockPos == null) {
                    this.owner.getNavigation().moveTo(path, 1.0);
                    return false;
                }
                this.posToMine = blockPos;
                return true;
            } else {
                this.owner.getNavigation().moveTo(path, 1.0);
            }
        }
        return false;
    }

    @Override
    public void start() {
        ((Nox$MiningInterface) this.owner).nox$setMining(true);
        this.target = this.owner.getTarget();
    }

    public boolean canContinueTouse() {
        if (this.target != null && this.target.isAlive() && this.owner.isAlive()) {
            if (this.posToMine != null && this.owner.distanceToSqr(this.posToMine.getX(), this.posToMine.getY(), this.posToMine.getZ()) <= 25.0D) {
                Path path = this.owner.getNavigation().createPath(target, 0);
                return path == null || !path.canReach();
            }
        }
        return false;
    }

    @Override
    public void stop() {
        ((Nox$MiningInterface) this.owner).nox$setMining(false);
        this.mineTick = 0;
        if (this.posToMine != null) {
            this.owner.level().destroyBlockProgress(this.owner.getId(), this.posToMine, -1);
        }
        this.posToMine = null;
    }

    @Override
    public void tick() {
        if (this.posToMine == null || !isBreakable(this.owner.level(), this.posToMine)) {
            this.mineTick = 0;
            stop();
            return;
        }
        float f = getAdjustedHardness(this.owner.level(), this.posToMine) * (float) (mineTick + 1);
        int k = (int) (f * 10.0F);
        this.mineTick++;
        this.owner.getNavigation().stop();
        this.owner.getLookControl().setLookAt(this.posToMine.getX() + 0.5D, this.posToMine.getY() + 0.5D, this.posToMine.getZ() + 0.5D);
        this.owner.stopUsingItem();
        if (this.mineTick % 5 == 0) {
            this.owner.swing(InteractionHand.MAIN_HAND);
        }
        this.owner.level().destroyBlockProgress(this.owner.getId(), this.posToMine, k);
        if (f > 1.0F) {
            this.owner.level().destroyBlock(this.posToMine, true, this.owner);
        }
    }

    public @Nullable BlockPos searchForBlock(Level world, BlockPos origin) {
        if (isBreakable(world, origin)) {
            return origin;
        } else if (isBreakable(world, origin.above())) {
            return origin.above();
        } else if (isBreakable(world, origin.below())) {
            return origin.below();
        }
        return null;
    }

    private boolean isBreakable(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);/*
        if (NoxConfig.respectBlockBreakingProtectionMods) {
            if (this.profile == null) {
                this.profile = new GameProfile(this.owner.getUUID(), "[" + this.owner.getType().getDescription() + "]");
            }
            if (!CommonProtection.canBreakBlock(world, pos, this.profile, null)) {
                return false;
            }
        }*/

        return !state.getCollisionShape(world, pos).isEmpty() && canMine(state);
    }
}
