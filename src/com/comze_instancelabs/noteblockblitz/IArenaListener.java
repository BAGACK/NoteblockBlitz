package com.comze_instancelabs.noteblockblitz;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comze_instancelabs.minigamesapi.ArenaListener;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArenaListener extends ArenaListener {

	Main plugin;

	public IArenaListener(Main plugin, PluginInstance pinstance) {
		super(plugin, pinstance, "noteblockblitz", new ArrayList<String>(Arrays.asList("/nb")));
		this.plugin = plugin;
	}

	@Override
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player p = event.getEntity();
		if (MinigamesAPI.getAPI().pinstances.get(plugin).global_players.containsKey(p.getName())) {
			event.getDrops().clear();
			p.setHealth(20D);
			for (PotionEffect t : p.getActivePotionEffects()) {
				if (t != null) {
					p.removePotionEffect(t.getType());
				}
			}
			if (p.getInventory().contains(Material.DIAMOND_AXE)) {
				ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
				ItemMeta itemmeta_axe = axe.getItemMeta();
				itemmeta_axe.addEnchant(Enchantment.KNOCKBACK, 2, true);
				itemmeta_axe.addEnchant(Enchantment.DIG_SPEED, 1, true);
				itemmeta_axe.setDisplayName(plugin.hammer_item);
				axe.setItemMeta(itemmeta_axe);
				p.getWorld().dropItem(p.getLocation(), axe);

				p.setWalkSpeed(0.0F);
				p.setFoodLevel(5);
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -7));
				p.sendMessage(plugin.stunned_because_lost_hammer);
			}
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					Util.clearInv(p);
				}
			}, 10L);
			Util.teleportPlayerFixed(p, MinigamesAPI.getAPI().pinstances.get(plugin).global_players.get(p.getName()).getSpawns().get(0));

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
