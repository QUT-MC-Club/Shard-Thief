package io.github.haykam821.shardthief.game;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class ShardInventoryManager {
	private static final int SHARD_HOLDER_ARMOR_COLOR = 0x649683;
	public static final ItemStack SHARD_HOLDER_HELMET = ShardInventoryManager.createArmorStack(Items.LEATHER_HELMET);
	public static final ItemStack SHARD_HOLDER_CHESTPLATE = ShardInventoryManager.createArmorStack(Items.LEATHER_CHESTPLATE);
	public static final ItemStack SHARD_HOLDER_LEGGINGS = ShardInventoryManager.createArmorStack(Items.LEATHER_LEGGINGS);
	public static final ItemStack SHARD_HOLDER_BOOTS = ShardInventoryManager.createArmorStack(Items.LEATHER_BOOTS);
	public static final ItemStack SHARD_HOLDER_SHARD = new ItemStack(Items.PRISMARINE_SHARD);

	public static final ItemStack NON_SHARD_HOLDER_BOW = ShardInventoryManager.createBowStack(Items.BOW);
	public static final ItemStack NON_SHARD_HOLDER_ARROW = new ItemStack(Items.ARROW);

	private static void updateInventory(ServerPlayerEntity player) {
		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());
	}

	public static void giveShardInventory(ServerPlayerEntity player) {
		player.getInventory().armor.set(3, SHARD_HOLDER_HELMET.copy());
		player.getInventory().armor.set(2, SHARD_HOLDER_CHESTPLATE.copy());
		player.getInventory().armor.set(1, SHARD_HOLDER_LEGGINGS.copy());
		player.getInventory().armor.set(0, SHARD_HOLDER_BOOTS.copy());
	
		for (int slot = 0; slot < 9; slot++) {
			player.getInventory().setStack(slot, SHARD_HOLDER_SHARD.copy());
		}

		ShardInventoryManager.updateInventory(player);
	}

	public static void giveNonShardInventory(ServerPlayerEntity player) {
		player.getInventory().setStack(0, NON_SHARD_HOLDER_BOW.copy());
		ShardInventoryManager.updateInventory(player);
	}

	public static void restockArrows(ServerPlayerEntity player, int maxArrows) {
		int arrows = player.getInventory().count(NON_SHARD_HOLDER_ARROW.getItem());
		if (arrows <= maxArrows) {
			player.getInventory().clear();
			ShardInventoryManager.giveNonShardInventory(player);

			ItemStack arrowStack = NON_SHARD_HOLDER_ARROW.copyWithCount(arrows + 1);
			player.getInventory().setStack(1, arrowStack);

			player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
		}
	}

	private static ItemStack createArmorStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(Enchantments.BINDING_CURSE, 1)
			.setDyeColor(SHARD_HOLDER_ARMOR_COLOR)
			.setUnbreakable()
			.build();
	}

	private static ItemStack createBowStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(Enchantments.MENDING, 1)
			.setUnbreakable()
			.build();
	}
}