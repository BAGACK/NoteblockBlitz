package com.comze_instancelabs.noteblockblitz;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

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
	static int max_minutes_per_game = 30;

	HashMap<String, BukkitTask> psneak = new HashMap<String, BukkitTask>();

	ArrayList<String> stunned = new ArrayList<String>();
	HashMap<String, Integer> gold = new HashMap<String, Integer>();

	IArenaScoreboard scoreboard;

	String hammerstr = ChatColor.GOLD + "Hit others to stun them! Hold shift for a knockback splash attack.";
	String hammer_item = "";
	String stunned_because_lost_hammer = "";
	String intro_message = "";
	String stunnedstr = "";

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "noteblockblitz", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), true);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.arenaSetup = new ArenaSetup();
		scoreboard = new IArenaScoreboard(this);
		pinstance.scoreboardManager = scoreboard;

		IArenaListener t = new IArenaListener(this, pinstance);
		api.registerArenaListenerLater(this, t);
		pinstance.setArenaListener(t);

		pli = pinstance;

		getConfig().addDefault("config.global_arenas_square_size", 5);
		getConfig().addDefault("config.max_minutes_per_game", 10);
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		global_arenas_size = getConfig().getInt("config.global_arenas_square_size");
		max_minutes_per_game = getConfig().getInt("config.max_minutes_per_game");

		pli.getMessagesConfig().getConfig().addDefault("messages.hammer_str", "&6Hit others to stun them! Hold shift for a knockback splash attack.");
		pli.getMessagesConfig().getConfig().addDefault("messages.hammer_item", "&4&lHammer");
		pli.getMessagesConfig().getConfig().addDefault("messages.stunned_because_lost_hammer", "&cYou are stunned because you lost the hammer!");
		pli.getMessagesConfig().getConfig().addDefault("messages.intro_message", "&c&lJump under noteblocks or break them to get gold! First one with 50 gold wins! Be aware of the hammer guy..");
		pli.getMessagesConfig().getConfig().addDefault("messages.stunned", "&cYou are stunned!");

		pli.getMessagesConfig().getConfig().options().copyDefaults(true);
		pli.getMessagesConfig().saveConfig();

		hammerstr = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.hammer_str"));
		hammer_item = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.hammer_item"));
		stunned_because_lost_hammer = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.stunned_because_lost_hammer"));
		intro_message = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.intro_message"));
		stunnedstr = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.stunned"));
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
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			if (stunned.contains(p.getName())) {
				event.setCancelled(true);
				return;
			}
			if (event.getItem().getItemStack().getType() == Material.GOLD_INGOT) {
				if (gold.containsKey(p.getName())) {
					gold.put(p.getName(), gold.get(p.getName()) + event.getItem().getItemStack().getAmount());
				} else {
					gold.put(p.getName(), event.getItem().getItemStack().getAmount());
				}
				IArena a = (IArena) pli.global_players.get(p.getName());
				a.updateScore(p.getName(), event.getItem().getItemStack().getAmount());
			} else if (event.getItem().getItemStack().getType() == Material.DIAMOND_AXE) {
				p.sendMessage(hammerstr);
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100000, 1));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 1));
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
				// Player jumping or at least he's in the air
				Block b = p.getLocation().getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP);
				if (b.getType() == Material.NOTE_BLOCK) {
					NoteBlock nb = (NoteBlock) b.getState();
					final Location l = nb.getLocation();
					IArena a = (IArena) pli.global_players.get(p.getName());
					if (a.getArenaState() != ArenaState.INGAME) {
						return;
					}

					nb.play();

					l.getWorld().dropItemNaturally(l, new ItemStack(Material.GOLD_INGOT));

					if (!a.nblocs.containsKey(l)) {
						if (Math.random() * 10 > 9 && a.nblocs_h.keySet().size() < 1) {
							a.nblocs_h.put(l, true);
						}
						a.nblocs.put(l, 1);
						a.nblocs_r.put(l, (int) (Math.random() * 3 + 5));
						event.setCancelled(true);
					} else {
						a.nblocs.put(l, a.nblocs.get(l) + 1);

						// hammer
						if (Math.random() * 2 > 1 && a.nblocs_h.containsKey(l)) {
							l.getWorld().strikeLightningEffect(l);
							final ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
							ItemMeta itemmeta_axe = axe.getItemMeta();
							itemmeta_axe.addEnchant(Enchantment.KNOCKBACK, 2, true);
							itemmeta_axe.addEnchant(Enchantment.DIG_SPEED, 1, true);
							itemmeta_axe.setDisplayName(hammer_item);
							axe.setItemMeta(itemmeta_axe);
							Bukkit.getScheduler().runTaskLater(this, new Runnable() {
								public void run() {
									l.getWorld().dropItem(l, axe);
								}
							}, 10L);
							b.setType(Material.AIR);
							return;
						}

						if (a.nblocs.get(l) > a.nblocs_r.get(l)) {
							b.setType(Material.AIR);
							return;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.NOTE_BLOCK) {
			Player p = event.getPlayer();
			if (pli.global_players.containsKey(p.getName())) {
				NoteBlock nb = (NoteBlock) event.getBlock().getState();
				final Location l = nb.getLocation();
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() != ArenaState.INGAME) {
					event.setCancelled(true);
					return;
				}

				nb.play();

				l.getWorld().dropItemNaturally(l, new ItemStack(Material.GOLD_INGOT));

				if (!a.nblocs.containsKey(l)) {
					if (Math.random() * 10 > 9 && a.nblocs_h.keySet().size() < 1) {
						a.nblocs_h.put(l, true);
					}
					a.nblocs.put(l, 1);
					a.nblocs_r.put(l, (int) (Math.random() * 3 + 6));
					event.setCancelled(true);
				} else {
					a.nblocs.put(l, a.nblocs.get(l) + 1);

					// hammer
					if (Math.random() * 2 > 1 && a.nblocs_h.containsKey(l)) {
						l.getWorld().strikeLightningEffect(l);
						final ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
						ItemMeta itemmeta_axe = axe.getItemMeta();
						itemmeta_axe.addEnchant(Enchantment.KNOCKBACK, 2, true);
						itemmeta_axe.addEnchant(Enchantment.DIG_SPEED, 1, true);
						itemmeta_axe.setDisplayName(hammer_item);
						axe.setItemMeta(itemmeta_axe);
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								l.getWorld().dropItem(l, axe);
							}
						}, 10L);
						// p.getInventory().addItem(axe);
						// p.sendMessage(hammerstr);
						// p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100000, 1));
						// p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 1));
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						return;
					} else {
						// if (Math.random() * 15 > 1) {
						// l.getWorld().createExplosion(l, 1.5F, false);
						// }
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
		if (event.getEntity() instanceof Player) {
			if (event.getDamager() instanceof Player) {
				final Player p = (Player) event.getEntity();
				Player attacker = (Player) event.getDamager();
				if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
					if (attacker.getItemInHand().getType() == Material.DIAMOND_AXE) {
						p.setWalkSpeed(0.0F);
						p.setFoodLevel(5);
						event.setDamage(0D);
						p.setHealth(20D);
						p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -7));
						p.sendMessage(stunnedstr);
						stunned.add(p.getName());
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								if (p != null) {
									stunned.remove(p.getName());
									p.setWalkSpeed(0.2F);
									p.setFoodLevel(20);
									p.removePotionEffect(PotionEffectType.JUMP);
								}
							}
						}, 80L);
					}
				}
			} else if (event.getDamager() instanceof LightningStrike) {
				final Player p = (Player) event.getEntity();
				if (pli.global_players.containsKey(p.getName())) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			if (event.isSneaking()) {
				if (!p.getInventory().contains(Material.DIAMOND_AXE)) {
					return;
				}
				if (!psneak.containsKey(p.getName())) {
					p.setExp(0.001F);
					psneak.put(p.getName(), Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
						public void run() {
							p.setExp(p.getExp() + 0.05F);
							if (p.getExp() > 0.9F) {
								p.setExp(0.99F);
								// do the knockback attack
								for (Entity e : p.getNearbyEntities(3.5D, 3.5D, 3.5D)) {
									if (e instanceof Player) {
										Player p_ = (Player) e;
										if (p != p_) {
											p_.setVelocity(p_.getEyeLocation().getDirection().multiply(-2D));
											p.playEffect(p_.getLocation(), Effect.POTION_BREAK, 5);
										}
									}
								}
								psneak.get(p.getName()).cancel();
							}
						}
					}, 3L, 3L));
				}
			} else {
				if (psneak.containsKey(p.getName())) {
					p.setExp(0.001F);
					// stopped sneaking, stop task
					if (psneak.get(p.getName()) != null) {
						psneak.get(p.getName()).cancel();
					}
					psneak.remove(p.getName());
				}
			}
		}
	}

}
