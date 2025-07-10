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
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scirave.nox.util.NoxUtil;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NoxBlockTagsProvider extends BlockTagsProvider {

    public NoxBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        /*this.getOrCreateRawBuilder(BlockTags.SWORD_EFFICIENT)
                .addElement(Nox.NOX_COBWEB.getId())
        ;

        this.getOrCreateRawBuilder(BlockTags.FALL_DAMAGE_RESETTING)
                .addElement(Nox.NOX_COBWEB.getId())
        ;*/

        this.getOrCreateRawBuilder(NoxUtil.NOX_ALWAYS_MINE)
                .addOptionalTag(BlockTags.WOODEN_DOORS.location())
                .addOptionalTag(BlockTags.WOODEN_TRAPDOORS.location())
                .addOptionalTag(BlockTags.PLANKS.location())
                .addOptionalTag(BlockTags.OVERWORLD_NATURAL_LOGS.location())
        ;

        this.getOrCreateRawBuilder(NoxUtil.NOX_CANT_MINE)
                .addOptionalTag(BlockTags.BANNERS.location())
                .addOptionalTag(BlockTags.BUTTONS.location())
                .addOptionalTag(BlockTags.CLIMBABLE.location())
                .addOptionalTag(BlockTags.CROPS.location())
                .addOptionalTag(BlockTags.PRESSURE_PLATES.location())
                .addOptionalTag(BlockTags.RAILS.location())
                .addOptionalTag(BlockTags.REPLACEABLE.location())
                .addOptionalTag(BlockTags.SAPLINGS.location())
                .addOptionalTag(BlockTags.FLOWERS.location())
                .addOptionalTag(BlockTags.ALL_SIGNS.location())
                .addElement(ResourceLocation.fromNamespaceAndPath("minecraft", "tinted_glass"))
        ;

    }
}
