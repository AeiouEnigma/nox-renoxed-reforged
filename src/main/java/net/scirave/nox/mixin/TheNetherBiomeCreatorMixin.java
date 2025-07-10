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

import net.minecraft.data.worldgen.biome.NetherBiomes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.scirave.nox.config.NoxConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherBiomes.class)
public class TheNetherBiomeCreatorMixin {

    @Redirect(method = "netherWastes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;addSpawn(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;)Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;", ordinal = 0))
    private static MobSpawnSettings.Builder nox$adjustNetherWastesSpawns(MobSpawnSettings.Builder instance, MobCategory MobCategory, MobSpawnSettings.SpawnerData SpawnerData) {
        if (NoxConfig.blazeNaturalSpawn) {
            instance.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3));
        }
        return instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, Mth.ceil(SpawnerData.getWeight().asInt() * 1.5), SpawnerData.minCount, SpawnerData.maxCount));
    }

    @Redirect(method = "basaltDeltas", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;addSpawn(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;)Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;", ordinal = 0))
    private static MobSpawnSettings.Builder nox$adjustBasaltDeltasSpawns(MobSpawnSettings.Builder instance, MobCategory MobCategory, MobSpawnSettings.SpawnerData SpawnerData) {
        if (NoxConfig.blazeNaturalSpawn) {
            instance.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 20, 1, 3));
        }
        return instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, Mth.ceil(SpawnerData.getWeight().asInt() * 1.5), SpawnerData.minCount, SpawnerData.maxCount * 4));
    }

    @Redirect(method = "crimsonForest", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;addSpawn(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;)Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;", ordinal = 0))
    private static MobSpawnSettings.Builder nox$adjustCrimsonForestSpawns(MobSpawnSettings.Builder instance, MobCategory MobCategory, MobSpawnSettings.SpawnerData SpawnerData) {
        if (NoxConfig.blazeNaturalSpawn) {
            instance.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 3, 1, 3));
        }
        instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 60, 1, 4));
        if (NoxConfig.spawnGhastsInMoreBiomes)
            instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, NoxConfig.increaseGhastSpawns ? 60 : 40, 1, 4));
        return instance.addSpawn(MobCategory, SpawnerData);
    }

    @Redirect(method = "warpedForest", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;addSpawn(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;)Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;", ordinal = 0))
    private static MobSpawnSettings.Builder nox$adjustWarpedForestSpawns(MobSpawnSettings.Builder instance, MobCategory MobCategory, MobSpawnSettings.SpawnerData SpawnerData) {
        if (NoxConfig.witherSkeletonsSpawnNaturallyInNether)
            instance.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 40, 1, 4));
        if (NoxConfig.spawnGhastsInMoreBiomes)
            instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, NoxConfig.increaseGhastSpawns ? 30 : 20, 1, 4));
        return instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 100, 4, 4));
    }

    @Redirect(method = "soulSandValley", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;addSpawn(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;)Lnet/minecraft/world/level/biome/MobSpawnSettings$Builder;", ordinal = 1))
    private static MobSpawnSettings.Builder nox$adjustSoulSandValleySpawns(MobSpawnSettings.Builder instance, MobCategory MobCategory, MobSpawnSettings.SpawnerData SpawnerData) {
        if (NoxConfig.witherSkeletonsSpawnNaturallyInNether)
            instance.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 5, 1, 4));
        return instance.addSpawn(MobCategory, new MobSpawnSettings.SpawnerData(EntityType.GHAST, Mth.ceil(SpawnerData.getWeight().asInt() * (NoxConfig.increaseGhastSpawns ? 1.5 : 1)), SpawnerData.minCount, SpawnerData.maxCount));
    }

}
