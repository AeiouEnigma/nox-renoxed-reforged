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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.Vec3;
import net.scirave.nox.util.Nox$EnderDragonFightInterface;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(EndDragonFight.class)
public abstract class EnderDragonFightMixin implements Nox$EnderDragonFightInterface {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Nullable
    private UUID dragonUUID;

    @Shadow
    private boolean dragonKilled;

    @Override
    public boolean isDragonKilled() {
        return this.dragonKilled;
    }

    @Override
    public boolean isConnectedCrystal(EndCrystal crystal) {
        Entity entity = level.getEntity(dragonUUID);
        if (entity instanceof EnderDragon dragon) {
            return dragon.nearestCrystal == crystal;
        }
        return false;
    }

    @Override
    public boolean inDragonRange(Vec3 pos) {
        if (!this.dragonKilled) {
            Entity entity = level.getEntity(dragonUUID);
            if (entity instanceof EnderDragon) {
                return entity.distanceToSqr(pos) < 250000.0D;
            }
        }
        return false;
    }

}
