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

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class Nox$FleeSunlightGoal extends Goal {

    protected final PathfinderMob mob;
    protected final double speed;
    protected final Level world;
    protected Path path;

    public Nox$FleeSunlightGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.world = mob.level();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        if (!this.world.isDay()) {
            return false;
        } else if (!this.mob.isOnFire()) {
            return false;
        } else if (!this.world.canSeeSky(this.mob.blockPosition())) {
            return false;
        } else if (!this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        } else {
            if (this.path != null && isShadedPos(this.path.getTarget())) {
                Path pathCheck = this.mob.getNavigation().createPath(this.path.getTarget(), 0);
                if (pathCheck != null && pathCheck.canReach()) {
                    this.path = pathCheck;
                    return true;
                }
            }
            return this.targetShadedPos();
        }
    }

    protected boolean isShadedPos(BlockPos pos) {
        return !this.world.canSeeSky(pos) && this.world.isEmptyBlock(pos);
    }

    protected boolean targetShadedPos() {
        Path maybePath = this.locateShadedPos();
        if (maybePath != null && maybePath.canReach()) {
            this.path = maybePath;
            return true;
        }
        return false;
    }

    public void tick() {
        this.mob.getNavigation().moveTo(this.path, this.speed);
    }

    @Nullable
    protected Path locateShadedPos() {
        RandomSource random = this.mob.getRandom();
        BlockPos blockPos = this.mob.blockPosition();

        for (int i = 0; i < 50; ++i) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(50) - 25, random.nextInt(8) - 4, random.nextInt(50) - 25);
            if (isShadedPos(blockPos2)) {
                return this.mob.getNavigation().createPath(blockPos2, 0);
            }
        }

        return null;
    }


}
