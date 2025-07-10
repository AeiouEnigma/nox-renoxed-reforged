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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.scirave.nox.config.NoxConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinEntityMixin extends HostileEntityMixin {

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    @Shadow
    protected abstract void alertOthers();

    @Override
    public void nox$maybeAngerOnShove(Player player) {
        super.nox$maybeAngerOnShove(player);
        this.alertOthers();
    }

    @Override
    public boolean nox$isAllowedToMine() {
        return NoxConfig.zombifiedPiglinsBreakBlocks;
    }

}
