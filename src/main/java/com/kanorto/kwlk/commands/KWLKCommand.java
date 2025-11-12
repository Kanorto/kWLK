package com.kanorto.kwlk.commands;

import com.kanorto.kwlk.KWLKPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Command handler for /kwlk
 */
public class KWLKCommand implements CommandExecutor, TabCompleter {
    
    private final KWLKPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, Long> pendingConfirmations;
    
    public KWLKCommand(KWLKPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.pendingConfirmations = new HashMap<>();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        // Check permission
        if (!sender.hasPermission("kwlk.use")) {
            sender.sendMessage(Component.text("You don't have permission to use this command."));
            return true;
        }
        
        // Handle subcommands
        if (args.length == 0) {
            // Show confirmation prompt
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();
                
                // Store pending confirmation
                long currentTime = System.currentTimeMillis();
                pendingConfirmations.put(playerId, currentTime);
                
                // Schedule confirmation expiry
                int timeout = plugin.getConfig().getInt("confirmation-timeout", 30);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Long confirmTime = pendingConfirmations.get(playerId);
                    if (confirmTime != null && confirmTime == currentTime) {
                        pendingConfirmations.remove(playerId);
                        String expiredMsg = plugin.getConfig().getString("expired-message", 
                            "<red>Confirmation expired. Please run the command again.</red>");
                        player.sendMessage(miniMessage.deserialize(expiredMsg));
                    }
                }, timeout * 20L);
                
                // Send confirmation message
                String confirmMsg = plugin.getConfig().getString("confirmation-message",
                    "<yellow>Are you sure you want to kick all players without permission? Type <green>/kwlk confirm</green> to proceed or <red>/kwlk cancel</red> to cancel.</yellow>");
                player.sendMessage(miniMessage.deserialize(confirmMsg));
            } else {
                sender.sendMessage(Component.text("This command can only be used by players."));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("confirm")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("This command can only be used by players."));
                return true;
            }
            
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            // Check if there's a pending confirmation
            if (!pendingConfirmations.containsKey(playerId)) {
                String expiredMsg = plugin.getConfig().getString("expired-message", 
                    "<red>Confirmation expired. Please run the command again.</red>");
                player.sendMessage(miniMessage.deserialize(expiredMsg));
                return true;
            }
            
            // Remove pending confirmation
            pendingConfirmations.remove(playerId);
            
            // Kick all players without permission (async preparation, sync execution)
            player.sendMessage(Component.text("§eProcessing kick request..."));
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                // Prepare list of players to kick (async)
                Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
                List<Player> playersToKick = new ArrayList<>();
                List<Player> playersToWhitelist = new ArrayList<>();
                
                for (Player p : onlinePlayers) {
                    if (!p.hasPermission("kwlk.bypass")) {
                        playersToKick.add(p);
                    } else {
                        playersToWhitelist.add(p);
                    }
                }
                
                // Execute kicks on main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    String kickMessageConfig = plugin.getConfig().getString("kick-message",
                        "<red><bold>You have been kicked from the server!</bold></red>\n<gray>Reason: Whitelist purge</gray>");
                    Component kickMessage = miniMessage.deserialize(kickMessageConfig);
                    
                    // Add to whitelist
                    for (Player p : playersToWhitelist) {
                        addToWhitelist(p);
                    }
                    
                    // Kick players
                    for (Player p : playersToKick) {
                        p.kick(kickMessage);
                    }
                    
                    // Send success message
                    String successMsg = plugin.getConfig().getString("success-message",
                        "<green>Successfully kicked <count> player(s) without permission.</green>");
                    successMsg = successMsg.replace("<count>", String.valueOf(playersToKick.size()));
                    sender.sendMessage(miniMessage.deserialize(successMsg));
                });
            });
            
            return true;
        } else if (subCommand.equals("cancel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("This command can only be used by players."));
                return true;
            }
            
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            // Remove pending confirmation
            pendingConfirmations.remove(playerId);
            
            // Send cancel message
            String cancelMsg = plugin.getConfig().getString("cancel-message",
                "<red>Kick operation cancelled.</red>");
            player.sendMessage(miniMessage.deserialize(cancelMsg));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Adds a player to the server whitelist
     */
    private void addToWhitelist(Player player) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        if (!offlinePlayer.isWhitelisted()) {
            offlinePlayer.setWhitelisted(true);
            plugin.getLogger().info("Added " + player.getName() + " to whitelist (has bypass permission)");
        }
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                     @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("confirm");
            completions.add("cancel");
            return completions;
        }
        return Collections.emptyList();
    }
}
