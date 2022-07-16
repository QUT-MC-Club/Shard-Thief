package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;

public class ShardThiefCountBar {
	private static final Formatting TITLE_COLOR = Formatting.AQUA;

	private final BossBarWidget bar;

	public ShardThiefCountBar(Text title, GlobalWidgets widgets) {
		this.bar = widgets.addBossBar(title.copy().formatted(TITLE_COLOR), BossBar.Color.BLUE, BossBar.Style.PROGRESS);
	}

	public void tick(ShardThiefActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setProgress(percent);
	}
}
