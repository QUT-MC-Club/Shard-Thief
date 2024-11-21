package io.github.haykam821.shardthief.game;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

public class ShardInventoryManager {
	private static final int SHARD_HOLDER_ARMOR_COLOR = 0x649683;

	public static final ItemStack SHARD_HOLDER_SHARD = new ItemStack(Items.PRISMARINE_SHARD);
	public static final ItemStack NON_SHARD_HOLDER_ARROW = new ItemStack(Items.ARROW);

	public final ItemStack shardHolderHelmet;
	public final ItemStack shardHolderChestplate;
	public final ItemStack shardHolderLeggings;
	public final ItemStack shardHolderBoots;

	public final ItemStack nonShardHolderBow;

	public ShardInventoryManager(RegistryWrapper.WrapperLookup registries) {
		this.shardHolderHelmet = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_HELMET);
		this.shardHolderChestplate = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_CHESTPLATE);
		this.shardHolderLeggings = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_LEGGINGS);
		this.shardHolderBoots = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_BOOTS);

		this.nonShardHolderBow = ShardInventoryManager.createBowStack(registries, Items.BOW);
	}

	private static void updateInventory(ServerPlayerEntity player) {
		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());
	}

	public void giveShardInventory(ServerPlayerEntity player) {
		player.getInventory().armor.set(3, this.shardHolderHelmet.copy());
		player.getInventory().armor.set(2, this.shardHolderChestplate.copy());
		player.getInventory().armor.set(1, this.shardHolderLeggings.copy());
		player.getInventory().armor.set(0, this.shardHolderBoots.copy());
	
		for (int slot = 0; slot < 9; slot++) {
			player.getInventory().setStack(slot, SHARD_HOLDER_SHARD.copy());
		}

		ShardInventoryManager.updateInventory(player);
	}

	public void giveNonShardInventory(ServerPlayerEntity player) {
		player.getInventory().setStack(0, this.nonShardHolderBow.copy());
		ShardInventoryManager.updateInventory(player);
	}

	public void restockArrows(ServerPlayerEntity player, int maxArrows) {
		int arrows = player.getInventory().count(NON_SHARD_HOLDER_ARROW.getItem());
		if (arrows <= maxArrows) {
			player.getInventory().clear();
			this.giveNonShardInventory(player);

			ItemStack arrowStack = NON_SHARD_HOLDER_ARROW.copyWithCount(arrows + 1);
			player.getInventory().setStack(1, arrowStack);

			player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
		}
	}

	private static ItemStack createArmorStack(RegistryWrapper.WrapperLookup registries, ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(registries, Enchantments.BINDING_CURSE, 1)
			.setDyeColor(SHARD_HOLDER_ARMOR_COLOR)
			.setUnbreakable()
			.build();
	}

	private static ItemStack createBowStack(RegistryWrapper.WrapperLookup registries, ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(registries, Enchantments.MENDING, 1)
			.setUnbreakable()
			.build();
	}
}