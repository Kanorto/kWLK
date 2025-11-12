package com.kanorto.kwlk.listeners;

import com.kanorto.kwlk.KWLKPlugin;
import com.kanorto.kwlk.managers.GhostManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Handles respawn item usage for reviving ghosts
 */
public class RespawnItemListener implements Listener {
    
    private final KWLKPlugin plugin;
    private final GhostManager ghostManager;
    private final MiniMessage miniMessage;
    
    public RespawnItemListener(KWLKPlugin plugin, GhostManager ghostManager) {
        this.plugin = plugin;
        this.ghostManager = ghostManager;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if respawn item feature is enabled
        if (!plugin.getConfig().getBoolean("respawn-item.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if player is holding an item
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        // Check if this is a respawn item
        if (!isRespawnItem(item)) {
            return;
        }
        
        // Cancel the event to prevent normal item usage
        event.setCancelled(true);
        
        // Run revival logic asynchronously to find ghosts, then sync for player operations
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int radius = plugin.getConfig().getInt("respawn-item.search-radius", 5);
            Player ghostToRevive = findNearestGhost(player.getLocation(), radius);
            
            // Switch back to sync thread for player operations
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (ghostToRevive != null) {
                    // Revive the ghost
                    reviveGhost(player, ghostToRevive);
                    
                    // Break the item
                    breakItem(player, item);
                } else {
                    // No ghosts found
                    String message = plugin.getConfig().getString("respawn-item.no-ghosts-message",
                        "<red>No ghosts found within <radius> blocks!</red>");
                    message = message.replace("<radius>", String.valueOf(radius));
                    player.sendMessage(miniMessage.deserialize(message));
                }
            });
        });
    }
    
    /**
     * Check if an item is a respawn item
     */
    private boolean isRespawnItem(ItemStack item) {
        String materialName = plugin.getConfig().getString("respawn-item.material", "TOTEM_OF_UNDYING");
        Material expectedMaterial;
        
        try {
            expectedMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid respawn item material: " + materialName);
            return false;
        }
        
        if (item.getType() != expectedMaterial) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String expectedName = plugin.getConfig().getString("respawn-item.display-name",
            "<gold><bold>Totem of Revival</bold></gold>");
        Component expectedComponent = miniMessage.deserialize(expectedName);
        
        return meta.displayName().equals(expectedComponent);
    }
    
    /**
     * Find the nearest ghost within radius
     */
    private Player findNearestGhost(Location center, int radius) {
        Set<UUID> ghostIds = ghostManager.getGhostPlayers();
        Player nearestGhost = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (UUID ghostId : ghostIds) {
            Player ghost = plugin.getServer().getPlayer(ghostId);
            if (ghost != null && ghost.isOnline()) {
                Location ghostLoc = ghost.getLocation();
                
                // Check if ghost is in the same world
                if (!ghostLoc.getWorld().equals(center.getWorld())) {
                    continue;
                }
                
                double distance = ghostLoc.distance(center);
                if (distance <= radius && distance < nearestDistance) {
                    nearestGhost = ghost;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestGhost;
    }
    
    /**
     * Revive a ghost player
     */
    private void reviveGhost(Player reviver, Player ghost) {
        // Remove ghost status
        ghostManager.removeGhost(ghost);
        
        // Send messages
        String reviverMessage = plugin.getConfig().getString("respawn-item.success-message",
            "<green>You have revived <player>!</green>");
        reviverMessage = reviverMessage.replace("<player>", ghost.getName());
        reviver.sendMessage(miniMessage.deserialize(reviverMessage));
        
        String revivedMessage = plugin.getConfig().getString("respawn-item.revived-message",
            "<green>You have been revived by <reviver>!</green>");
        revivedMessage = revivedMessage.replace("<reviver>", reviver.getName());
        ghost.sendMessage(miniMessage.deserialize(revivedMessage));
    }
    
    /**
     * Break the respawn item
     */
    private void breakItem(Player player, ItemStack item) {
        // Reduce item amount by 1
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            // Remove the item entirely
            player.getInventory().remove(item);
        }
        
        // Play item break sound
        player.getWorld().playSound(player.getLocation(), 
            org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
    }
}
