package com.noahbelstad.keepsomeinventory;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

import java.io.File;
import java.util.Set;

public class KeepSomeInventory implements ModInitializer {
	public static final String MOD_ID = "keepsome-inventory";
	private static KeepSomeInventoryConfig CONFIG;

	public static final GameRule<Boolean> RULE_DO_KEEP_SOME_INVENTORY = GameRuleBuilder
			.forBoolean(true)
			.category(GameRuleCategory.PLAYER)
			.buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "do_keep_some_inventory"));

	@Override
	public void onInitialize() {
		CONFIG = KeepSomeInventoryConfig.load();
		registerCommands();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			boolean doKeepSomeInventory = server.overworld().getGameRules().get(RULE_DO_KEEP_SOME_INVENTORY);

			if (doKeepSomeInventory) {
				server.overworld().getGameRules().set(GameRules.KEEP_INVENTORY, true, server);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof ServerPlayer player)) return;
			if (!(player.level() instanceof ServerLevel level)) return;

			boolean ruleEnabled = level.getGameRules().get(RULE_DO_KEEP_SOME_INVENTORY);
			if (!ruleEnabled) return;

			Set<Item> whitelist = CONFIG.getResolvedWhitelist();

			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack stack = player.getInventory().getItem(i);
				if (stack.isEmpty()) continue;

				if (whitelist.contains(stack.getItem())) {
					continue;
				}

				int originalCount = stack.getCount();
				int dropCount = 0;

				for (int j = 0; j < originalCount; j++) {
					if (Math.random() < CONFIG.dropChance) {
						dropCount++;
					}
				}

				if (dropCount > 0) {
					ItemStack dropStack = stack.copy();
					dropStack.setCount(dropCount);

					ItemEntity itemEntity = new ItemEntity(
							level,
							player.getX(),
							player.getY(),
							player.getZ(),
							dropStack
					);
					level.addFreshEntity(itemEntity);

					stack.shrink(dropCount);
				}
			}
		});
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("keepsome")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))

					// --- RELOAD COMMAND ---
					.then(Commands.literal("reload")
							.executes(context -> {
								CONFIG = KeepSomeInventoryConfig.load();
								context.getSource().sendSuccess(() -> Component.literal("§aKeepSomeInventory configuration reloaded!"), true);
								return 1;
							})
					)

					// --- CONFIG (OPEN FILE) COMMAND ---
					.then(Commands.literal("config")
							.executes(context -> {
								// Check if the server is a dedicated server
								if (context.getSource().getServer().isDedicatedServer()) {
									context.getSource().sendFailure(Component.literal("§cThis command can only be used in Singleplayer (Integrated Server)!"));
									return 0; // Return 0 to indicate the command failed
								}

								File configFile = FabricLoader.getInstance().getConfigDir().resolve("keepsome-inventory.json").toFile();

								if (configFile.exists()) {
									Util.getPlatform().openFile(configFile);
									context.getSource().sendSuccess(() -> Component.literal("§aOpening the config file..."), false);
								} else {
									context.getSource().sendFailure(Component.literal("§cConfig file does not exist yet!"));
								}
								return 1;
							})
					)

					// --- ADD COMMAND ---
					.then(Commands.literal("add")
							.executes(context -> { // No arguments: add held item
								ServerPlayer player = context.getSource().getPlayerOrException();
								ItemStack mainHand = player.getMainHandItem();
								if (mainHand.isEmpty()) {
									context.getSource().sendFailure(Component.literal("§cYou must hold an item or specify an item ID!"));
									return 0;
								}
								String id = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
								CONFIG.addWhitelistItem(id);
								context.getSource().sendSuccess(() -> Component.literal("§aAdded " + id + " to the KeepSomeInventory whitelist."), true);
								return 1;
							})
							.then(Commands.argument("itemid", StringArgumentType.word()) // Argument: add string ID
									.executes(context -> {
										String id = StringArgumentType.getString(context, "itemid");
										CONFIG.addWhitelistItem(id);
										context.getSource().sendSuccess(() -> Component.literal("§aAdded " + id + " to the KeepSomeInventory whitelist."), true);
										return 1;
									})
							)
					)

					// --- REMOVE COMMAND ---
					.then(Commands.literal("remove")
							.executes(context -> { // No arguments: remove held item
								ServerPlayer player = context.getSource().getPlayerOrException();
								ItemStack mainHand = player.getMainHandItem();
								if (mainHand.isEmpty()) {
									context.getSource().sendFailure(Component.literal("§cYou must hold an item or specify an item ID!"));
									return 0;
								}
								String id = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
								CONFIG.removeWhitelistItem(id);
								context.getSource().sendSuccess(() -> Component.literal("§eRemoved " + id + " from the KeepSomeInventory whitelist."), true);
								return 1;
							})
							.then(Commands.argument("itemid", StringArgumentType.word()) // Argument: remove string ID
									.executes(context -> {
										String id = StringArgumentType.getString(context, "itemid");
										CONFIG.removeWhitelistItem(id);
										context.getSource().sendSuccess(() -> Component.literal("§eRemoved " + id + " from the KeepSomeInventory whitelist."), true);
										return 1;
									})
							)
					)
			);
		});
	}
}