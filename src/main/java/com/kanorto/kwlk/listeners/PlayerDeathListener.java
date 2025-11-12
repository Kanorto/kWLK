package com.kanorto.kwlk.listeners;

import com.kanorto.kwlk.KWLKPlugin;
import com.kanorto.kwlk.managers.GhostManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private final MiniMessage miniMessage;
    
    public PlayerDeathListener(KWLKPlugin plugin, GhostManager ghostManager) {
        this.plugin = plugin;
        this.ghostManager = ghostManager;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is already a ghost (second death)
        if (ghostManager.isGhost(player.getUniqueId())) {
            // Ghost died again - kick them if enabled
            if (plugin.getConfig().getBoolean("ghost-mode.kick-on-second-death", true)) {
                String kickMessage = plugin.getConfig().getString("ghost-mode.ghost-kick-message",
                    "<red><bold>You died as a ghost!</bold></red>\n<gray>Ghosts cannot die twice.</gray>");
                Component message = miniMessage.deserialize(kickMessage);
                
                // Schedule kick on next tick to avoid issues during death event
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.kick(message);
                });
            }
        } else {
            // First death - make player a ghost
            ghostManager.makeGhost(player);
        }
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
