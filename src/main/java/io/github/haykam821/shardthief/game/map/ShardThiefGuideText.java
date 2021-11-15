package io.github.haykam821.shardthief.game.map;

import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.AbstractHologram.VerticalAlign;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public final class ShardThiefGuideText {
	private static final Formatting GUIDE_FORMATTING = Formatting.GOLD;
	private static final Text[] GUIDE_LINES = {
		new TranslatableText("gameType.shardthief.shard_thief").formatted(GUIDE_FORMATTING).formatted(Formatting.BOLD),
		new TranslatableText("text.shardthief.guide.hold_shard").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.shardthief.guide.steal_shard").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.shardthief.guide.drop_shard").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.shardthief.guide.pick_up_shard").formatted(GUIDE_FORMATTING),
	};

	public static AbstractHologram spawn(ServerWorld world, Vec3d pos) {
		AbstractHologram hologram = Holograms.create(world, pos, GUIDE_LINES);
		hologram.setAlignment(VerticalAlign.CENTER);

		hologram.show();
		return hologram;
	}
}
