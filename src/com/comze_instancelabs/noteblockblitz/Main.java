package com.comze_instancelabs.noteblockblitz;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.commands.CommandHandler;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static Main m = null;
	static int global_arenas_size = 30;

	HashMap<String, String> lastdamager = new HashMap<String, String>();

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "noteblockblitz", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), false);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.arenaSetup = new ArenaSetup();
		pli = pinstance;

		IArenaListener t = new IArenaListener(this, pinstance);
		api.registerArenaListenerLater(this, t);
		pinstance.setArenaListener(t);
		
		getConfig().addDefault("config.global_arenas_square_size", 5);
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		global_arenas_size = getConfig().getInt("config.global_arenas_square_size");
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		a.setSquare(global_arenas_size);
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CommandHandler ch = new CommandHandler();
		return ch.handleArgs(this, "mgnoteblockblitz", "/" + cmd.getName(), sender, args);
	}

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			if (event.getItem().getItemStack().getType() != Material.DIAMOND_AXE) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.NOTE_BLOCK) {
			Player p = event.getPlayer();
			if (pli.global_players.containsKey(p.getName())) {
				NoteBlock nb = (NoteBlock) event.getBlock().getState();
				Location l = nb.getLocation();
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() != ArenaState.INGAME) {
					event.setCancelled(true);
					return;
				}
				a.updateScore(p.getName());
				if (!a.nblocs.containsKey(l)) {
					if (Math.random() * 14 > 13 && a.nblocs_h.keySet().size() < 1) {
						a.nblocs_h.put(l, true);
					}
					a.nblocs.put(l, 1);
					a.nblocs_r.put(l, (int) (Math.random() * 4 + 5));
					event.setCancelled(true);
				} else {
					a.nblocs.put(l, a.nblocs.get(l) + 1);

					// hammer
					if (Math.random() * 2 > 1 && a.nblocs_h.containsKey(l)) {
						ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
						ItemMeta itemmeta_axe = axe.getItemMeta();
						itemmeta_axe.addEnchant(Enchantment.KNOCKBACK, 5, true);
						p.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
						p.sendMessage(ChatColor.RED + "Hit others to stun them!");
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100000, 1));
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 1));
						a.updateScore(p.getName());
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						return;
					}

					if (a.nblocs.get(l) > a.nblocs_r.get(l)) {
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						return;
					} else {
						event.setCancelled(true);
					}
				}
				nb.setNote(new Note(nb.getNote().getId() + 1));
				nb.update();
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			Player p = event.getPlayer();
			if (pli.global_players.containsKey(p.getName())) {
				if (event.getClickedBlock().getType() == Material.NOTE_BLOCK) {
					if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
						event.setCancelled(true);
					} else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
						IArena a = (IArena) pli.global_players.get(p.getName());
						Location l = event.getClickedBlock().getLocation();
						if (a.nblocs_h.containsKey(l)) {
							p.playNote(l, Instrument.BASS_GUITAR, new Note(a.nblocs.get(l)));
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Player p = (Player) event.getEntity();
			Player attacker = (Player) event.getDamager();
			if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
				if (attacker.getItemInHand().getType() == Material.DIAMOND_AXE) {
					p.setWalkSpeed(0.0F);
					p.setFoodLevel(5);
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -7));
					p.sendMessage(ChatColor.RED + "You are stunned!");
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p.setWalkSpeed(0.2F);
							p.setFoodLevel(20);
							p.removePotionEffect(PotionEffectType.JUMP);
						}
					}, 60L);
				}
			}
		}
	}

}
