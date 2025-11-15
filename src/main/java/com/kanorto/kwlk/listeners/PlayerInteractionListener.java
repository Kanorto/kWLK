package com.kanorto.kwlk.listeners;

import com.kanorto.kwlk.managers.GhostManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

/**
 * Prevents ghost players from interacting with the world
 */
public class PlayerInteractionListener implements Listener {
    
    private final GhostManager ghostManager;
    
    public PlayerInteractionListener(GhostManager ghostManager) {
        this.ghostManager = ghostManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ghostManager.isGhost(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (ghostManager.isGhost(damager.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (ghostManager.isGhost(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
