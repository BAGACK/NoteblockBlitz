package com.comze_instancelabs.noteblockblitz;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinecraftVersionsType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;

public class IArenaScoreboard extends ArenaScoreboard {

	public HashMap<String, Scoreboard> ascore = new HashMap<String, Scoreboard>();
	HashMap<String, Objective> gpp = new HashMap<String, Objective>();

	Main plugin = null;

	public IArenaScoreboard(Main m) {
		this.plugin = m;
	}

	boolean temp_info = false;

	public void updateScoreboard(final IArena arena) {
		if (arena != null) {
			for (String p_ : arena.getAllPlayers()) {
				Player p = Bukkit.getPlayer(p_);
				if (!ascore.containsKey(p_)) {
					ascore.put(p_, Bukkit.getScoreboardManager().getNewScoreboard());
				}
				if (!gpp.containsKey(p_)) {
					if (p_.length() < 14) {
						gpp.put(p_, ascore.get(p_).registerNewObjective(p_ + "_2", "dummy"));
					} else {
						gpp.put(p_, ascore.get(p_).registerNewObjective(p_.subSequence(0, p_.length() - 1) + "-", "dummy"));
					}
				} else {
					gpp.get(p_).unregister();
					gpp.remove(p_);
					if (p_.length() < 14) {
						gpp.put(p_, ascore.get(p_).registerNewObjective(p_ + "_2", "dummy"));
					} else {
						gpp.put(p_, ascore.get(p_).registerNewObjective(p_.subSequence(0, p_.length() - 1) + "-", "dummy"));
					}
				}

				if (MinigamesAPI.SERVER_VERSION.isBelow(MinecraftVersionsType.V1_7_R4)) {
					if (!temp_info) {
						temp_info = true;
						this.plugin.getLogger().severe("No scoreboard support for 1.7.9 and below! Only 1.7.10 and higher versions.");
					}
					return;
				}

				gpp.get(p_).setDisplaySlot(DisplaySlot.SIDEBAR);
				gpp.get(p_).setDisplayName("[" + arena.getDisplayName() + "]");

				ascore.get(p_).resetScores(p_);
				if (!plugin.gold.containsKey(p_)) {
					plugin.gold.put(p_, 0);
				}

				int gp = 0;
				if (plugin.getConfig().isSet("player." + p_)) {
					gp = plugin.getConfig().getInt("player." + p_ + ".gp"); // +2 gp!
				}

				int c = 0;
				for (Map.Entry<String, Integer> entry : plugin.getTop(arena).entrySet()) {
					c++;
					if (c > 3) {
						break;
					}

					int i = 0;

					if (entry.getKey().length() > 0 && entry.getKey().length() < 17) {
						i++;
						if (i > 3) {
							break;
						}
						String name = entry.getKey();
						if (entry.getKey().length() > 12) {
							name = entry.getKey().substring(0, 12);
						}
						if (plugin.temp_gold.containsKey(entry.getKey())) {
							ascore.get(p_).resetScores(name + "(" + plugin.temp_gold.get(entry.getKey()) + ")");
							// p.sendMessage(name + "(" + plugin.temp_gold.get(entry.getKey()) + ")");
						} else {
							plugin.temp_gold.put(entry.getKey(), 0);
							ascore.get(p_).resetScores(name + "(" + plugin.temp_gold.get(entry.getKey()) + ")");
							// p.sendMessage(name + "(" + plugin.temp_gold.get(entry.getKey()) + ")");
						}
						if (plugin.gold.containsKey(entry.getKey())) {
							name += "(" + plugin.gold.get(entry.getKey()) + ")";
						} else {
							name = entry.getKey();
						}
						plugin.temp_gold.put(entry.getKey(), plugin.gold.get(entry.getKey()));
						gpp.get(p_).getScore(name).setScore(0 - c);
						// p.sendMessage(name);
					}
				}

				gpp.get(p_).getScore(" ").setScore(-7);
				gpp.get(p_).getScore("Hammer Guy").setScore(-8);
				if (!arena.currentHammerGuy.equalsIgnoreCase("")) {
					gpp.get(p_).getScore(arena.currentHammerGuy).setScore(-9);
				} else {
					gpp.get(p_).getScore("None").setScore(-9);
				}

				p.setScoreboard(ascore.get(p_));
			}
		}
	}

	@Override
	public void updateScoreboard(JavaPlugin plugin, final Arena arena) {
		IArena a = (IArena) MinigamesAPI.getAPI().pinstances.get(plugin).getArenaByName(arena.getName());
		this.updateScoreboard(a);
	}

	@Override
	public void removeScoreboard(String arena, Player p) {
		if (ascore.containsKey(p.getName())) {
			try {
				Scoreboard sc = ascore.get(p.getName());
				for (OfflinePlayer player : sc.getPlayers()) {
					sc.resetScores(player);
				}
			} catch (Exception e) {
				if (MinigamesAPI.debug) {
					MinigamesAPI.getAPI().getLogger().log(Level.WARNING, "exception", e);
				}
			}
		}
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sc = manager.getNewScoreboard();
		sc.clearSlot(DisplaySlot.SIDEBAR);
		p.setScoreboard(sc);
	}

}
