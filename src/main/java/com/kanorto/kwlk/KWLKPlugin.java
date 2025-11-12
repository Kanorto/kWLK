package com.kanorto.kwlk;

import com.kanorto.kwlk.commands.GiveRespawnCommand;
import com.kanorto.kwlk.commands.KWLKCommand;
import com.kanorto.kwlk.commands.SetRespawnCommand;
import com.kanorto.kwlk.listeners.PlayerDeathListener;
import com.kanorto.kwlk.listeners.PlayerInteractionListener;
import com.kanorto.kwlk.listeners.RespawnItemListener;
import com.kanorto.kwlk.managers.GhostManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for kWLK
 * Handles kicking players without permission and ghost mode functionality
 */
public class KWLKPlugin extends JavaPlugin {
    
    private GhostManager ghostManager;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        ghostManager = new GhostManager(this);
        
        // Register commands
        KWLKCommand kwlkCommand = new KWLKCommand(this);
        getCommand("kwlk").setExecutor(kwlkCommand);
        getCommand("kwlk").setTabCompleter(kwlkCommand);
        
        GiveRespawnCommand giveRespawnCommand = new GiveRespawnCommand(this);
        getCommand("giverespawn").setExecutor(giveRespawnCommand);
        getCommand("giverespawn").setTabCompleter(giveRespawnCommand);
        
        SetRespawnCommand setRespawnCommand = new SetRespawnCommand(this);
        getCommand("setrespawn").setExecutor(setRespawnCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, ghostManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractionListener(ghostManager), this);
        getServer().getPluginManager().registerEvents(new RespawnItemListener(this, ghostManager), this);
        
        getLogger().info("kWLK plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clear all ghost players on shutdown
        if (ghostManager != null) {
            ghostManager.clearAllGhosts();
        }
        
        getLogger().info("kWLK plugin has been disabled!");
    }
    
    public GhostManager getGhostManager() {
        return ghostManager;
    }
}
