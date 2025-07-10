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

package net.scirave.nox.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.scirave.nox.Nox;

public class NoxCobwebBlockEntity extends BlockEntity {

    private static final String AGE_NBT_KEY = "nox:cobweb_age";
    private static final short MAX_AGE = 200; // ticks

    private short age = 0;
    private byte ticksUntilRemovalCheck = 0;

    public NoxCobwebBlockEntity(BlockPos pos, BlockState state) {
        super(Nox.NOX_COBWEB_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState blockState, NoxCobwebBlockEntity be) {
        if (be.age > MAX_AGE) {
            if (be.ticksUntilRemovalCheck == 0) {
                be.ticksUntilRemovalCheck = 12;
                be.setChanged();
                if (world.getRandom().nextInt(20) == 0)
                    world.removeBlock(pos, false);
            } else {
                be.ticksUntilRemovalCheck--;
                be.setChanged();
            }
        } else {
            be.age++;
            be.setChanged();
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        this.age = tag.getShort(AGE_NBT_KEY);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putShort(AGE_NBT_KEY, this.age);
    }

    public static <T extends BlockEntity> void tickStart(Level world, BlockPos blockPos, BlockState blockState, T t) {

        tick(world, blockPos, blockState, (NoxCobwebBlockEntity)t);
    }
}
