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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.util.Nox$EnderDragonFightInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystal.class)
public abstract class EndCrystalEntityMixin extends EntityMixin {

    @Override
    public void nox$invulnerableCheck(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (this.level() instanceof ServerLevel serverWorld && NoxConfig.endCrystalsIndestructibleUnlessConnectedToDragon) {
            EndDragonFight enderDragonFight = serverWorld.getDragonFight();
            if (enderDragonFight != null && ((Nox$EnderDragonFightInterface) enderDragonFight).inDragonRange(((EndCrystal) (Object) this).position())) {
                if (!(((Nox$EnderDragonFightInterface) enderDragonFight).isConnectedCrystal((EndCrystal) (Object) this))) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

}
