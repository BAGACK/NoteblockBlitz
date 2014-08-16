package com.comze_instancelabs.noteblockblitz;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

	@Override
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player p = event.getEntity();
		if (MinigamesAPI.getAPI().pinstances.get(plugin).global_players.containsKey(p.getName())) {
			p.setHealth(20D);
			if (p.getInventory().contains(Material.DIAMOND_AXE)) {
				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.DIAMOND_AXE));
			}
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					Util.clearInv(p);
				}
			}, 10L);
			Util.teleportPlayerFixed(p, MinigamesAPI.getAPI().pinstances.get(plugin).global_players.get(p.getName()).getSpawns().get(0));

			p.setWalkSpeed(0.0F);
			p.setFoodLevel(5);
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -7));
			p.sendMessage(ChatColor.RED + "You are stunned because you lost the hammer!");
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					p.setWalkSpeed(0.2F);
					p.setFoodLevel(20);
					p.removePotionEffect(PotionEffectType.JUMP);
				}
			}, 60L);

			return;
		}
	}

}
