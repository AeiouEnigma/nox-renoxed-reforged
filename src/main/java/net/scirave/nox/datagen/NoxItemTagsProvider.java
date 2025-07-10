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

package net.scirave.nox.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scirave.nox.util.NoxUtil;

import java.util.concurrent.CompletableFuture;

public class NoxItemTagsProvider extends ItemTagsProvider {

    public NoxItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, null, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.getOrCreateRawBuilder(NoxUtil.FIREPROOF)
                .addElement(Items.MAGMA_CREAM.asItem().builtInRegistryHolder().getKey().location())
                .addElement(Items.WITHER_SKELETON_SKULL.asItem().builtInRegistryHolder().getKey().location())
                .addElement(Items.NETHER_STAR.asItem().builtInRegistryHolder().getKey().location())
                .addElement(Items.GHAST_TEAR.asItem().builtInRegistryHolder().getKey().location())
                .addElement(Items.BLAZE_ROD.asItem().builtInRegistryHolder().getKey().location())
                .addElement(Items.BLAZE_POWDER.asItem().builtInRegistryHolder().getKey().location())
        ;

        this.getOrCreateRawBuilder(NoxUtil.TOOLS)
                .addOptionalTag(ItemTags.SWORDS.location())
                .addOptionalTag(ItemTags.AXES.location())
                .addOptionalTag(ItemTags.PICKAXES.location())
                .addOptionalTag(ItemTags.SWORDS.location())
                .addOptionalTag(ItemTags.HOES.location())
        ;

        var armor = this.getOrCreateRawBuilder(NoxUtil.ARMOR);

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ArmorItem)
                armor.addElement(item.asItem().builtInRegistryHolder().getKey().location());
        }
    }
}
