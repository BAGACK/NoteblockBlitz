package com.comze_instancelabs.noteblockblitz;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	private BukkitTask timer;
	Main m = null;
	public int c = 5;
	BukkitTask tt;
	int currentingamecount;

	public HashMap<String, Integer> pscore = new HashMap<String, Integer>();
	public HashMap<Location, Integer> nblocs = new HashMap<Location, Integer>();
	public HashMap<Location, Integer> nblocs_r = new HashMap<Location, Integer>();
	public HashMap<Location, Integer> nblocs_h = new HashMap<Location, Integer>();

	public String currentHammerGuy = "";

	boolean flag = false;

	public IArena(Main m, String arena) {
		super(m, arena);
		this.m = m;
	}

	public void updateScore(String player, int count) {
		if (!pscore.containsKey(player)) {
			pscore.put(player, count);
			return;
		}
		int c = pscore.get(player) + count;
		if (c > 49) {
			for (String p : this.getAllPlayers()) {
				if (!p.equalsIgnoreCase(player)) {
					m.pli.global_lost.put(p, this);
				}
				if (m.gold.containsKey(p)) {
					m.gold.remove(p);
				}
				if (pscore.containsKey(p)) {
					pscore.remove(p);
				}
			}
			flag = true;
			this.stop();
			return;
		}
		pscore.put(player, c);
		m.pli.scoreboardManager.setCurrentScoreMap(pscore);
		m.pli.scoreboardManager.updateScoreboard(m, this);
	}

	public void setSquare(int i) {
		this.c = i;
	}

	public void generateArena(Location start) {
		int x = start.getBlockX() - (5 * (c / 2));
		int y = start.getBlockY() + 3;
		int z = start.getBlockZ() - (5 * (c / 2));

		Location s = new Location(start.getWorld(), x, y, z);

		// generate a c * c square of noteblocks

		for (int i = 0; i < c; i++) {
			for (int j = 0; j < c; j++) {
				Block b = s.clone().add(i * 5D, 0D, j * 5D).getBlock();
				b.setType(Material.NOTE_BLOCK);
				b.setData((byte) 0);
				if (Math.random() * 10 > 9 && this.nblocs_h.keySet().size() < 1) {
					this.nblocs_h.put(b.getLocation(), 0);
				}
				this.nblocs.put(b.getLocation(), 1);
				this.nblocs_r.put(b.getLocation(), (int) (Math.random() * 4 + 4));
			}
		}
	}

	@Override
	public void start(boolean tp) {
		super.start(tp);
		c = Main.global_arenas_size;
		generateArena(this.getSpawns().get(0));
		final IArena a = this;
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerOnline(p_)) {
						Player p = Bukkit.getPlayer(p_);
						p.setWalkSpeed(0.2F);
						p.setFoodLevel(20);
						p.removePotionEffect(PotionEffectType.JUMP);
					}
				}
			}
		}, 20L);
	}

	@Override
	public void started() {
		for (String p : this.getAllPlayers()) {
			this.updateScore(p, 0);
			if (Validator.isPlayerOnline(p)) {
				Bukkit.getPlayer(p).sendMessage(m.intro_message);
			}
		}
		final IArena a = this;
		tt = Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				a.stop();
			}
		}, 20L * 60 * m.max_minutes_per_game);
	}

	@Override
	public void spectate(String playername) {
		Util.teleportPlayerFixed(Bukkit.getPlayer(playername), this.getSpawns().get(0));
	}

	@Override
	public void stop() {
		if (!flag) {
			int highest = 0;
			String highest_p = "";
			for (String p : this.getAllPlayers()) {
				if (pscore.containsKey(p)) {
					if (pscore.get(p) > highest) {
						highest = pscore.get(p);
						highest_p = p;
					}
				}
				if (m.gold.containsKey(p)) {
					m.gold.remove(p);
				}
				if (m.temp_gold.containsKey(p)) {
					m.temp_gold.remove(p);
				}
				if (pscore.containsKey(p)) {
					pscore.remove(p);
				}
			}
			for (String p : this.getAllPlayers()) {
				if (!p.equalsIgnoreCase(highest_p)) {
					m.pli.global_lost.put(p, this);
				}
			}
		}
		super.stop();
		final IArena a = this;
		if (timer != null) {
			timer.cancel();
		}
		if (tt != null) {
			tt.cancel();
		}
		nblocs.clear();
		nblocs_r.clear();
		nblocs_h.clear();
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				c = Main.global_arenas_size;
				a.generateArena(a.getSpawns().get(0));
			}
		}, 10L);
	}
}
