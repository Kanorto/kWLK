package com.kanorto.kwlk.managers;

import com.kanorto.kwlk.KWLKPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages ghost mode for dead players
 */
public class GhostManager {
    
    private final KWLKPlugin plugin;
    private final Set<UUID> ghostPlayers;
    private final MiniMessage miniMessage;
    
    public GhostManager(KWLKPlugin plugin) {
        this.plugin = plugin;
        this.ghostPlayers = new HashSet<>();
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    /**
     * Checks if a player is a ghost
     */
    public boolean isGhost(UUID playerId) {
        return ghostPlayers.contains(playerId);
    }
    
    /**
     * Gets all ghost player UUIDs
     */
    public Set<UUID> getGhostPlayers() {
        return new HashSet<>(ghostPlayers);
    }
    
    /**
     * Makes a player a ghost
     */
    public void makeGhost(Player player) {
        if (!plugin.getConfig().getBoolean("ghost-mode.enabled", true)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        if (!ghostPlayers.contains(playerId)) {
            ghostPlayers.add(playerId);
            applyGhostEffects(player);
            
            // Give ghost permission
            player.addAttachment(plugin, "kwlk.ghost", true);
            
            // Send ghost message
            String ghostMessage = plugin.getConfig().getString("ghost-mode.ghost-message", 
                "<gray><italic>Вы умерли и стали призраком. Вы не можете взаимодействовать с миром.</italic></gray>");
            Component message = miniMessage.deserialize(ghostMessage);
            player.sendMessage(message);
            
            // Log to console
            plugin.getLogger().info("[GHOST] Игрок " + player.getName() + " (" + playerId + ") стал призраком");
        }
    }
    
    /**
     * Removes ghost status from a player
     */
    public void removeGhost(Player player) {
        UUID playerId = player.getUniqueId();
        if (ghostPlayers.remove(playerId)) {
            removeGhostEffects(player);
            
            // Remove ghost permission by reloading permissions
            player.recalculatePermissions();
            
            // Send respawn message
            String respawnMessage = plugin.getConfig().getString("ghost-mode.respawn-message", 
                "<green>Вы возродились и больше не призрак.</green>");
            Component message = miniMessage.deserialize(respawnMessage);
            player.sendMessage(message);
            
            // Log to console
            plugin.getLogger().info("[GHOST] Игрок " + player.getName() + " (" + playerId + ") больше не призрак");
        }
    }
    
    /**
     * Applies ghost effects to a player
     */
    private void applyGhostEffects(Player player) {
        // Make player invisible
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY,
            PotionEffect.INFINITE_DURATION,
            0,
            false,
            false,
            false
        ));
        
        // Ensure no flight
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
        }
        if (player.isFlying()) {
            player.setFlying(false);
        }
        
        // Set to adventure mode to prevent block breaking/placing
        player.setGameMode(GameMode.ADVENTURE);
    }
    
    /**
     * Removes ghost effects from a player
     */
    private void removeGhostEffects(Player player) {
        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        
        // Restore to survival mode (or maintain current if not adventure)
        if (player.getGameMode() == GameMode.ADVENTURE) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
    
    /**
     * Clears all ghosts (for plugin shutdown)
     */
    public void clearAllGhosts() {
        for (UUID playerId : new HashSet<>(ghostPlayers)) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeGhost(player);
            }
        }
        ghostPlayers.clear();
    }
}
