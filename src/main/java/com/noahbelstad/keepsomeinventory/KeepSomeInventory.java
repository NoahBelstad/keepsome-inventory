package com.noahbelstad.keepsomeinventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

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
}