package io.github.haykam821.shardthief.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerShardEntry implements Comparable<PlayerShardEntry> {
	private final ServerPlayerEntity player;
	private int counts;
	private int invulnerability;

	public PlayerShardEntry(ServerPlayerEntity player, int counts, int invulnerability) {
		this.player = player;
		this.counts = counts;
		this.invulnerability = invulnerability;
	}

	public PlayerShardEntry(ServerPlayerEntity player, int invulnerability) {
		this(player, 20, invulnerability);
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public int getCounts() {
		return this.counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public void decrementCounts() {
		this.counts -= 1;
	}

	public Text getWinMessage() {
		return this.getPlayer().getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
	}

	public Text getStealMessage() {
		MutableText playerName = this.getPlayer().getDisplayName().shallowCopy().formatted(Formatting.AQUA);
		return playerName.append(new LiteralText(" has stolen the shard!").formatted(Formatting.WHITE));
	}

	public void setInvulnerability(int invulnerability) {
		this.invulnerability = invulnerability;
	}

	public boolean canBeStolen() {
		return this.invulnerability <= 0;
	}

	public void tick() {
		if (this.invulnerability > 0) {
			this.invulnerability -= 1;
		}
	}

	@Override
	public int compareTo(PlayerShardEntry other) {
		return other.counts - this.counts;
	}

	@Override
	public String toString() {
		return "PlayerShardEntry{player=" + this.getPlayer() + ", counts=" + this.getCounts() + "}";
	}
}