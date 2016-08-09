package com.comze_instancelabs.noteblockblitz;

import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(Main m){
		super(m, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		
		// default (stone)
    	this.getConfig().addDefault("config.kits.default.name", "default");
    	this.getConfig().addDefault("config.kits.default.items", "373:16386*1");
    	this.getConfig().addDefault("config.kits.default.lore", "Speed I");
    	this.getConfig().addDefault("config.kits.default.requires_money", false);
    	this.getConfig().addDefault("config.kits.default.requires_permission", false);
    	this.getConfig().addDefault("config.kits.default.money_amount", 100);
    	this.getConfig().addDefault("config.kits.default.permission_node", MinigamesAPI.getAPI().getPermissionKitPrefix() + ".default");
    	
    	// iron
    	this.getConfig().addDefault("config.kits.iron.name", "iron");
    	this.getConfig().addDefault("config.kits.iron.items", "373:16482*1");
    	this.getConfig().addDefault("config.kits.iron.lore", "Speed II");
    	this.getConfig().addDefault("config.kits.iron.requires_money", false);
    	this.getConfig().addDefault("config.kits.iron.requires_permission", false);
    	this.getConfig().addDefault("config.kits.iron.money_amount", 100);
    	this.getConfig().addDefault("config.kits.iron.permission_node", MinigamesAPI.getAPI().getPermissionKitPrefix() + ".iron");

    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
	}
	
}
