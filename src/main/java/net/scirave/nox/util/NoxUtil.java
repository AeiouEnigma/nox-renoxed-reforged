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

package net.scirave.nox.util;

import com.ibm.icu.impl.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.scirave.nox.config.NoxConfig;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class NoxUtil {

    public static final TagKey<Block> NOX_ALWAYS_MINE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("nox", "always_mine"));
    public static final TagKey<Block> NOX_CANT_MINE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("nox", "cant_mine"));
    public static final TagKey<Item> FIREPROOF = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("nox", "fireproof"));
    public static final TagKey<Item> ARMOR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("nox", "mob_armor"));
    public static final TagKey<Item> TOOLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("nox", "mob_tools"));
    private final static ItemStack WOOD_PICKAXE = Items.WOODEN_PICKAXE.getDefaultInstance();
    private final static ItemStack WOOD_AXE = Items.WOODEN_AXE.getDefaultInstance();
    private final static ItemStack WOOD_SHOVEL = Items.WOODEN_SHOVEL.getDefaultInstance();

    public static boolean isAtWoodLevel(BlockState state) {
        return !state.requiresCorrectToolForDrops() || WOOD_PICKAXE.isCorrectToolForDrops(state) || WOOD_AXE.isCorrectToolForDrops(state) || WOOD_SHOVEL.isCorrectToolForDrops(state);
    }

    public static boolean isAnAlly(Mob attacker, Mob victim) {

        boolean validTypes = (attacker instanceof Enemy && victim instanceof Enemy) ||
                (attacker instanceof AbstractGolem && victim instanceof AbstractGolem);

        LivingEntity attackerTarget = attacker.getTarget();
        LivingEntity victimTarget = victim.getTarget();

        return NoxConfig.noFriendlyFire && validTypes && attackerTarget != attacker && victimTarget != victim && victimTarget != null && attackerTarget == victimTarget;
    }

    public static void EnderDragonShootFireball(EnderDragon dragon, LivingEntity target) {
        Vec3 i = dragon.getHeadLookVector(1.0F);
        double k = dragon.head.getX() - i.x;
        double l = dragon.head.getY(0.5D) + 0.5D;
        double m = dragon.head.getZ() - i.z;
        double n = target.getX() - k;
        double o = target.getY(0.5D) - l;
        double p = target.getZ() - m;
        if (!dragon.isSilent()) {
            dragon.level().levelEvent(null, 1017, dragon.blockPosition(), 0);
        }

        DragonFireball dragonFireballEntity = new DragonFireball(dragon.level(), dragon, new Vec3(n, o, p));
        dragonFireballEntity.moveTo(k, l, m, 0.0F, 0.0F);
        dragonFireballEntity.accelerationPower *= 5;
        dragon.level().addFreshEntity(dragonFireballEntity);
    }

    public static Item randomWeapon(RandomSource random) {
        return BuiltInRegistries.ITEM.getOrCreateTag(TOOLS).getRandomElement(random).map(Holder::value).orElse(Items.AIR);
    }

    public static Item randomArmor(RandomSource random) {
        return BuiltInRegistries.ITEM.getOrCreateTag(TOOLS).getRandomElement(random).map(Holder::value).orElse(Items.AIR);
        //Registries.ITEM.getOrCreateEntryList(ARMOR).getRandom(random).map(RegistryAccess.RegistryEntry::value).orElse(Items.AIR);
    }

    public static double getLeewayAmount(LivingEntity armorWearer, double damage, double total, int armor, double toughness, double modifier, DamageSource source) {
        double diff = damage * modifier - CombatRules.getDamageAfterAbsorb(armorWearer, (float) total, source, armor, (float) toughness);
        double ratio = 0;
        if (diff != 0) {
            ratio = diff / damage;
        }

        return ratio;
    }

    public static boolean resistanceWithinLeeway(LivingEntity armorWearer, double damage, double total, int armor, double toughness, double lowerLeeway, double higherLeeway, double modifier, DamageSource source) {
        double ratio = getLeewayAmount(armorWearer, damage, total, armor, toughness, modifier, source);
        return ratio >= -higherLeeway && ratio <= lowerLeeway;
    }

    public static double getItemQuality(Item item, EquipmentSlot slot, Holder<Attribute> type, @Nullable Double base) {
        if (base == null) {
            base = (double) 0;
        }

        var map = item.getDefaultAttributeModifiers();
        double multiple = 0;
        double multiply = 1;
        double add = 0;

        for (var entry : map.modifiers()) {
            var attribute = entry.attribute();
            var modifier = entry.modifier();

            if (attribute == type.value() && entry.slot().test(slot)) {
                switch (modifier.operation()) {
                    case ADD_MULTIPLIED_BASE -> multiple += modifier.amount();
                    case ADD_VALUE -> add += modifier.amount();
                    case ADD_MULTIPLIED_TOTAL -> multiply *= 1 + modifier.amount();
                }

            }
        }

        return (base + add + (base + add) * multiple) * (multiply);
    }

    public static double getItemDamage(Item item, EquipmentSlot slot, double baseDamage) {
        return getItemQuality(item, slot, Attributes.ATTACK_DAMAGE, baseDamage);
    }

    public static Pair<Integer, Float> getItemProtection(Item item, EquipmentSlot slot, double baseDamage) {
        int armor = (int) Math.floor(getItemQuality(item, slot, Attributes.ARMOR, (double) 0));
        float toughness = (float) getItemQuality(item, slot, Attributes.ARMOR_TOUGHNESS, (double) 0);

        return Pair.of(armor, toughness);
    }

    public static double getItemDPS(Item item, EquipmentSlot slot, double baseDamage, double attackSpeed) {
        return getItemQuality(item, slot, Attributes.ATTACK_DAMAGE, baseDamage) * getItemQuality(item, slot, Attributes.ATTACK_SPEED, attackSpeed);
    }

    public static double getBestPlayerDPS(Player player) {
        double defaultAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        double damage = 0;
        Inventory inventory = player.getInventory();

        for (int i = 0; i < 8; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                double potential = getItemDPS(stack.getItem(), EquipmentSlot.MAINHAND, 1, defaultAttackSpeed);
                if (potential > damage) {
                    damage = potential;
                }
            }
        }

        ItemStack stack = inventory.getItem(Inventory.SLOT_OFFHAND);
        if (!stack.isEmpty()) {
            double potential = getItemDPS(stack.getItem(), EquipmentSlot.MAINHAND, 1, defaultAttackSpeed);
            if (potential > damage) {
                damage = potential;
            }
        }

        return damage;
    }

    public static void weaponRoulette(ServerLevel world, Mob mob, RandomSource random, DifficultyInstance difficulty) {
        Player player = world.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), 128, true);
        if (player != null) {
            int luck = Mth.nextInt(random, 1, 4);
            boolean freeFirstPass = luck == 1;
            boolean noWeapon = luck == 4;

            if (noWeapon) return;

            int armor = player.getArmorValue();
            float toughness = (float) mob.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            double damage = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double total = damage;

            double mod = player.getMaxHealth() / Math.max(mob.getMaxHealth(), 20);
            float clamped = difficulty.getEffectiveDifficulty();

            double lowerLeeway = 0.50;
            lowerLeeway -= lowerLeeway * 0.5 * clamped;

            double higherLeeway = 0.25;
            higherLeeway += higherLeeway * 4 * clamped;

            Item item = null;
            int iterated = 0;

            while (freeFirstPass || !resistanceWithinLeeway(mob, damage, total, armor, toughness, lowerLeeway, higherLeeway, mod, world.damageSources().generic()) && iterated < 20) {
                freeFirstPass = false;
                for (int i = 0; i < 5; i++) {
                    item = randomWeapon(random);

                    total = getItemDamage(item, EquipmentSlot.MAINHAND, damage);

                    if (resistanceWithinLeeway(mob, damage, total, armor, toughness, lowerLeeway, higherLeeway, mod, world.damageSources().generic())) {
                        break;
                    }
                }

                lowerLeeway += 0.1;
                higherLeeway += 0.1;
                iterated++;
            }

            if (item != null) {
                ItemStack stack = item.getDefaultInstance();
                mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
        }
    }

    public static void armorRoulette(ServerLevel world, Mob mob, RandomSource random, DifficultyInstance difficulty) {
        Player player = world.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), 64, true);
        if (player != null) {
            int luck = Mth.nextInt(random, 1, 4);
            boolean freeFirstPass = luck == 1;
            boolean noArmor = luck == 4;

            if (noArmor) return;

            double modifier = Math.max(mob.getMaxHealth(), 20) / player.getMaxHealth();
            double damage = 4 * 2;
            double total = Math.max((getBestPlayerDPS(player) / 3) * 2, damage);

            int armor = 0;
            float toughness = 0;

            float clamped = difficulty.getEffectiveDifficulty();

            double lowerLeeway = 0.25;
            lowerLeeway += lowerLeeway * 4 * clamped;

            double higherLeeway = 0.50;
            higherLeeway -= higherLeeway * 0.5 * clamped;

            HashSet<ArmorItem> armorItems = new HashSet<>();
            int iterated = 0;

            while (freeFirstPass || !resistanceWithinLeeway(mob, damage, total, armor, toughness, lowerLeeway, higherLeeway, modifier, world.damageSources().generic()) && iterated < 20) {
                freeFirstPass = false;
                for (int i = 0; i < 5; i++) {
                    double lastLeeway = getLeewayAmount(mob, damage, total, armor, toughness, modifier, world.damageSources().generic());
                    Item item = randomArmor(random);

                    if (item instanceof ArmorItem armorItem) {
                        EquipmentSlot slot = armorItem.getEquipmentSlot();

                        ArmorItem toRemove = null;
                        for (ArmorItem potential : armorItems) {
                            if (potential.getEquipmentSlot() == slot) {
                                toRemove = potential;
                                break;
                            }
                        }

                        if (toRemove != null) {
                            armor -= toRemove.getDefense();
                            toughness -= toRemove.getToughness();
                        }

                        armor += armorItem.getDefense();
                        toughness += armorItem.getToughness();

                        double newLeeway = getLeewayAmount(mob, damage, total, armor, toughness, modifier, world.damageSources().generic());
                        if ((newLeeway <= 0 && (newLeeway - lastLeeway) >= 0)
                                || resistanceWithinLeeway(mob, damage, total, armor, toughness, lowerLeeway, higherLeeway, modifier, world.damageSources().generic())) {
                            if (toRemove != null) {
                                armorItems.remove(toRemove);
                            }
                            armorItems.add(armorItem);
                        } else {
                            armor -= armorItem.getDefense();
                            toughness -= armorItem.getToughness();
                            if (toRemove != null) {
                                armor += toRemove.getDefense();
                                toughness += toRemove.getToughness();
                            }
                        }
                    }

                    if (resistanceWithinLeeway(mob, damage, total, armor, toughness, lowerLeeway, higherLeeway, modifier, world.damageSources().generic())) {
                        break;
                    }
                }

                lowerLeeway += 0.1;
                higherLeeway += 0.1;
                iterated++;
            }

            if (!armorItems.isEmpty()) {
                for (ArmorItem item : armorItems) {
                    mob.setItemSlot(item.getEquipmentSlot(), item.getDefaultInstance());
                }
            }
        }
    }

}
