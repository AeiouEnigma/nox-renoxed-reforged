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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.Nox$CreeperBreachInterface;

import java.util.EnumSet;

public class Nox$CreeperBreachGoal extends Goal {

    private final Creeper creeper;

    public Nox$CreeperBreachGoal(Creeper creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        if (((Nox$CreeperBreachInterface) creeper).nox$isAllowedToBreachWalls()) {
            LivingEntity living = this.creeper.getTarget();
            return living != null && living.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && shouldBreach(living);
        }
        return false;
    }
    public boolean withinReach(Vec3 pos, LivingEntity target) {
        double yDiff = Math.abs(pos.y - target.getY());
        return yDiff <= NoxConfig.creeperBreachDistance;
    }

    private boolean shouldBreach(LivingEntity living) {
        if (!creeper.isPathFinding() && this.creeper.tickCount > 60 && (this.creeper.onGround() || !this.creeper.isInWater())) {
            Path path = creeper.getNavigation().createPath(living, 0);
            if (path == null) {
                return withinReach(this.creeper.position(), living);
            } else if (!path.canReach() && path.getEndNode() != null && path.getEndNode().distanceToSqr(this.creeper.blockPosition()) <= 4) {
                return withinReach(path.getEndNode().asBlockPos().getCenter(), living);
            } else {
                creeper.getNavigation().moveTo(path, 1.0);
            }
        }
        return false;
    }

    public void start() {
        this.creeper.ignite();
    }
}
