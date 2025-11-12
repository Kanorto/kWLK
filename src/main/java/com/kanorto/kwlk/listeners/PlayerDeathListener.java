package com.kanorto.kwlk.listeners;

import com.kanorto.kwlk.KWLKPlugin;
import com.kanorto.kwlk.managers.GhostManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles player death and respawn events for ghost mode
 */
public class PlayerDeathListener implements Listener {
    
    private final KWLKPlugin plugin;
    private final GhostManager ghostManager;
    
    public PlayerDeathListener(KWLKPlugin plugin, GhostManager ghostManager) {
        this.plugin = plugin;
        this.ghostManager = ghostManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        
        // Make player a ghost when they die
        ghostManager.makeGhost(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Remove ghost status when player respawns
        // Delay by 1 tick to ensure player is fully respawned
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ghostManager.removeGhost(player);
        }, 1L);
    }
}
