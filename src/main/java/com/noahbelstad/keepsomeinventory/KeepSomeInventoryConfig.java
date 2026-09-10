package com.noahbelstad.keepsomeinventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeepSomeInventoryConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("keepsome-inventory.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public double dropChance = 0.75;

    // Wrapped in an ArrayList to ensure it is mutable when we add/remove via commands
    public List<String> whitelist = new ArrayList<>(List.of(
            // --- Netherite Tier ---
            "minecraft:netherite_sword",
            "minecraft:netherite_pickaxe",
            "minecraft:netherite_axe",
            "minecraft:netherite_shovel",
            "minecraft:netherite_hoe",
            "minecraft:netherite_spear",
            "minecraft:netherite_helmet",
            "minecraft:netherite_chestplate",
            "minecraft:netherite_leggings",
            "minecraft:netherite_boots",

            // --- Diamond Tier ---
            "minecraft:diamond_sword",
            "minecraft:diamond_pickaxe",
            "minecraft:diamond_axe",
            "minecraft:diamond_shovel",
            "minecraft:diamond_hoe",
            "minecraft:diamond_spear",
            "minecraft:diamond_helmet",
            "minecraft:diamond_chestplate",
            "minecraft:diamond_leggings",
            "minecraft:diamond_boots",

            // --- Iron Tier ---
            "minecraft:iron_sword",
            "minecraft:iron_pickaxe",
            "minecraft:iron_axe",
            "minecraft:iron_shovel",
            "minecraft:iron_hoe",
            "minecraft:iron_spear",
            "minecraft:iron_helmet",
            "minecraft:iron_chestplate",
            "minecraft:iron_leggings",
            "minecraft:iron_boots",

            // --- Copper Tier ---
            "minecraft:copper_sword",
            "minecraft:copper_pickaxe",
            "minecraft:copper_axe",
            "minecraft:copper_shovel",
            "minecraft:copper_hoe",
            "minecraft:copper_spear",
            "minecraft:copper_helmet",
            "minecraft:copper_chestplate",
            "minecraft:copper_leggings",
            "minecraft:copper_boots",

            // --- Golden Tier ---
            "minecraft:golden_sword",
            "minecraft:golden_pickaxe",
            "minecraft:golden_axe",
            "minecraft:golden_shovel",
            "minecraft:golden_hoe",
            "minecraft:golden_spear",
            "minecraft:golden_helmet",
            "minecraft:golden_chestplate",
            "minecraft:golden_leggings",
            "minecraft:golden_boots",

            // --- Stone Tier ---
            "minecraft:stone_sword",
            "minecraft:stone_pickaxe",
            "minecraft:stone_axe",
            "minecraft:stone_shovel",
            "minecraft:stone_hoe",
            "minecraft:stone_spear",

            // --- Wooden Tier ---
            "minecraft:wooden_sword",
            "minecraft:wooden_pickaxe",
            "minecraft:wooden_axe",
            "minecraft:wooden_shovel",
            "minecraft:wooden_hoe",
            "minecraft:wooden_spear",

            // --- Chainmail & Leather Armor ---
            "minecraft:chainmail_helmet",
            "minecraft:chainmail_chestplate",
            "minecraft:chainmail_leggings",
            "minecraft:chainmail_boots",
            "minecraft:leather_helmet",
            "minecraft:leather_chestplate",
            "minecraft:leather_leggings",
            "minecraft:leather_boots",
            "minecraft:turtle_helmet",

            // --- Weapons & Special Combat ---
            "minecraft:bow",
            "minecraft:crossbow",
            "minecraft:trident",
            "minecraft:mace",
            "minecraft:shield",

            // --- Utilities & Mobility ---
            "minecraft:elytra",
            "minecraft:shears",
            "minecraft:flint_and_steel",
            "minecraft:fishing_rod",
            "minecraft:brush",
            "minecraft:spyglass",
            "minecraft:carrot_on_a_stick",
            "minecraft:warped_fungus_on_a_stick"
    ));

    public static KeepSomeInventoryConfig load() {
        KeepSomeInventoryConfig config = new KeepSomeInventoryConfig();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                KeepSomeInventoryConfig loaded = GSON.fromJson(reader, KeepSomeInventoryConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save(config);
        }
        return config;
    }

    public static void save(KeepSomeInventoryConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWhitelistItem(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        if (!whitelist.contains(id)) {
            whitelist.add(id);
            save(this);
        }
    }

    public void removeWhitelistItem(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        if (whitelist.contains(id)) {
            whitelist.remove(id);
            save(this);
        }
    }

    public Set<Item> getResolvedWhitelist() {
        Set<Item> items = new HashSet<>();
        if (whitelist == null) return items;

        for (String id : whitelist) {
            Identifier location = Identifier.tryParse(id);
            if (location != null) {
                BuiltInRegistries.ITEM.getOptional(location).ifPresent(items::add);
            }
        }
        return items;
    }
}