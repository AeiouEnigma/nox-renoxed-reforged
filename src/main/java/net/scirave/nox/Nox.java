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

package net.scirave.nox;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scirave.nox.blocks.NoxCobwebBlock;
import net.scirave.nox.config.NoxConfig;
import net.scirave.nox.blocks.NoxCobwebBlockEntity;
import net.scirave.nox.datagen.NoxBlockTagsProvider;
import net.scirave.nox.datagen.NoxItemTagsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(Nox.MOD_ID)
public class Nox {

    public static final String MOD_ID = "nox";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    //public static final DeferredBlock<Block> NOX_COBWEB = BLOCKS.registerBlock("cobweb", NoxCobwebBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB));
    public static final NoxCobwebBlock NOX_COBWEB = Registry.register(BuiltInRegistries.BLOCK, "nox:cobweb", new NoxCobwebBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB)));
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Nox.MOD_ID);
    public static final Supplier<BlockEntityType<NoxCobwebBlockEntity>> NOX_COBWEB_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "cobweb_block_entity", () -> BlockEntityType.Builder.of(NoxCobwebBlockEntity::new, NOX_COBWEB).build(null));

    public Nox(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        NoxConfig.init(MOD_ID, NoxConfig.class);
        NoxConfig.write(MOD_ID);
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                // Tell generator to run only when server data are generating
                event.includeServer(),
                // Extends net.neoforged.neoforge.common.data.BlockTagsProvider
                (DataProvider.Factory<NoxBlockTagsProvider>) output -> new NoxBlockTagsProvider(
                        output,
                        event.getLookupProvider(),
                        MOD_ID,
                        event.getExistingFileHelper()
                )
        );
        event.getGenerator().addProvider(
                // Tell generator to run only when server data are generating
                event.includeServer(),
                // Extends net.neoforged.neoforge.common.data.BlockTagsProvider
                (DataProvider.Factory<NoxItemTagsProvider>) output -> new NoxItemTagsProvider(
                        output,
                        event.getLookupProvider(),
                        MOD_ID,
                        event.getExistingFileHelper()
                )
        );
    }

}
