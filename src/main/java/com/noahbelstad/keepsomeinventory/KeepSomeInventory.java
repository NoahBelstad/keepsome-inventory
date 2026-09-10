package com.noahbelstad.keepsomeinventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class KeepSomeInventory implements ModInitializer {
	public static final String MOD_ID = "keepsome-inventory";
	private static KeepSomeInventoryConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = KeepSomeInventoryConfig.load();

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof ServerPlayer player)) return;

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
							player.level(),
							player.getX(),
							player.getY(),
							player.getZ(),
							dropStack
					);
					player.level().addFreshEntity(itemEntity);

					stack.shrink(dropCount);
				}
			}
		});
	}
}