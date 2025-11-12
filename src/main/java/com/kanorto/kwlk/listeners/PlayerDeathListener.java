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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is already a ghost (second death)
        if (ghostManager.isGhost(player.getUniqueId())) {
            plugin.getLogger().info("[GHOST] Призрак " + player.getName() + " умер второй раз");
            
            // Ghost died again - kick them if enabled
            if (plugin.getConfig().getBoolean("ghost-mode.kick-on-second-death", true)) {
                String kickMessage = plugin.getConfig().getString("ghost-mode.ghost-kick-message",
                    "<red><bold>Вы умерли будучи призраком!</bold></red>\n<gray>Призраки не могут умереть дважды.</gray>");
                Component message = miniMessage.deserialize(kickMessage);
                
                plugin.getLogger().info("[GHOST] Кикаем призрака " + player.getName() + " за вторую смерть");
                
                // Schedule kick on next tick to avoid issues during death event
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.kick(message);
                });
            }
        } else {
            plugin.getLogger().info("[GHOST] Игрок " + player.getName() + " умер первый раз");
            // First death - make player a ghost (permission only, effects applied on respawn)
            ghostManager.makeGhost(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Check if ghost mode is enabled and player is a ghost
        if (!plugin.getConfig().getBoolean("ghost-mode.enabled", true)) {
            return;
        }
        
        if (ghostManager.isGhost(player.getUniqueId())) {
            plugin.getLogger().info("[GHOST] Игрок " + player.getName() + " возрождается как призрак");
            
            // Apply ghost effects after player is fully respawned
            // Delay by 2 ticks to ensure player is fully respawned
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (ghostManager.isGhost(player.getUniqueId())) {
                    ghostManager.applyGhostEffects(player);
                }
            }, 2L);
        } else {
            plugin.getLogger().info("[GHOST] Игрок " + player.getName() + " возрождается (не призрак)");
        }
    }
}
