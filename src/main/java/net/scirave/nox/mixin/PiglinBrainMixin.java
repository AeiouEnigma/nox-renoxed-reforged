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

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "isWearingGold", at = @At("RETURN"), cancellable = true)
    private static void nox$piglinWearingAllGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (NoxConfig.piglinsRequireExclusivelyGoldArmor) {
            if (cir.getReturnValue()) {
                Iterable<ItemStack> iterable = entity.getArmorSlots();
                Iterator<ItemStack> iterator = iterable.iterator();

                boolean hasGoldenArmor = false;

                while (iterator.hasNext()) {
                    ItemStack stack = iterator.next();
                    Item item = stack.getItem();
                    if (item instanceof ArmorItem armor) {
                        if (armor.getMaterial() == ArmorMaterials.GOLD || item.builtInRegistryHolder().is(ItemTags.PIGLIN_LOVED)) {
                            hasGoldenArmor = true;
                        } else {
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }

                cir.setReturnValue(hasGoldenArmor);
            }
        }
    }

}
