package com.comze_instancelabs.noteblockblitz;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.ArenaListener;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArenaListener extends ArenaListener {

	JavaPlugin plugin;

	public IArenaListener(JavaPlugin plugin, PluginInstance pinstance) {
		super(plugin, pinstance, "noteblockblitz", new ArrayList<String>(Arrays.asList("/nb")));
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		if (MinigamesAPI.getAPI().pinstances.get(plugin).global_players.containsKey(p.getName())) {
			if (p.getInventory().contains(Material.DIAMOND_AXE)) {
				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.DIAMOND_AXE));
				event.getEntity().setHealth(20D);
				Util.teleportPlayerFixed(p, MinigamesAPI.getAPI().pinstances.get(plugin).global_players.get(p).getSpawns().get(0));
				return;
			}
		}
	}

}
