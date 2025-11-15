package com.kanorto.kwlk.commands;

import com.kanorto.kwlk.KWLKPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import java.util.concurrent.ConcurrentHashMap;

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
        this.pendingConfirmations = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        // Check permission
        if (!sender.hasPermission("kwlk.use")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды.");
            return true;
        }
        
        // Handle subcommands
        if (args.length == 0) {
            // Show confirmation prompt
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();
                
                plugin.getLogger().info("[KICK] Игрок " + player.getName() + " запустил команду кика");
                
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
                            "<red>Время подтверждения истекло. Пожалуйста, запустите команду снова.</red>");
                        player.sendMessage(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(expiredMsg)));
                        plugin.getLogger().info("[KICK] Время подтверждения истекло для " + player.getName());
                    }
                }, timeout * 20L);
                
                // Send confirmation message
                String confirmMsg = plugin.getConfig().getString("confirmation-message",
                    "<yellow>Вы уверены, что хотите кикнуть всех игроков без прав? Напишите <green>/kwlk confirm</green> для подтверждения или <red>/kwlk cancel</red> для отмены.</yellow>");
                player.sendMessage(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(confirmMsg)));
            } else {
                sender.sendMessage("§cЭту команду могут использовать только игроки.");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("confirm")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cЭту команду могут использовать только игроки.");
                return true;
            }
            
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            // Check if there's a pending confirmation
            if (!pendingConfirmations.containsKey(playerId)) {
                String expiredMsg = plugin.getConfig().getString("expired-message", 
                    "<red>Время подтверждения истекло. Пожалуйста, запустите команду снова.</red>");
                player.sendMessage(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(expiredMsg)));
                return true;
            }
            
            // Remove pending confirmation
            pendingConfirmations.remove(playerId);
            
            // Kick all players without permission (sync permission checks, async processing)
            player.sendMessage("§eОбработка запроса на кик...");
            plugin.getLogger().info("[KICK] Игрок " + player.getName() + " подтвердил команду кика");
            
            // Do all checks on main thread for thread safety
            Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
            List<Player> playersToKick = new ArrayList<>();
            List<Player> playersToWhitelist = new ArrayList<>();
            
            plugin.getLogger().info("[KICK] Проверка прав для " + onlinePlayers.size() + " игроков...");
            
            for (Player p : onlinePlayers) {
                // IMPORTANT: Check bypass permission before kicking (on main thread for thread safety)
                if (!p.hasPermission("kwlk.bypass")) {
                    playersToKick.add(p);
                    plugin.getLogger().info("[KICK] Игрок " + p.getName() + " будет кикнут (нет права kwlk.bypass)");
                } else {
                    playersToWhitelist.add(p);
                    plugin.getLogger().info("[KICK] Игрок " + p.getName() + " защищен (есть право kwlk.bypass)");
                }
            }
            
            // Execute kicks (still on main thread as required by Bukkit API)
            String kickMessageConfig = plugin.getConfig().getString("kick-message",
                "<red><bold>Вы были кикнуты с сервера!</bold></red>\n<gray>Причина: Чистка вайтлиста</gray>");
            String kickMessage = LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(kickMessageConfig));
            
            // Add to whitelist
            for (Player p : playersToWhitelist) {
                addToWhitelist(p);
            }
            
            // Kick players
            for (Player p : playersToKick) {
                p.kickPlayer(kickMessage);
                plugin.getLogger().info("[KICK] Игрок " + p.getName() + " был кикнут");
            }
            
            plugin.getLogger().info("[KICK] Завершено. Кикнуто: " + playersToKick.size() + ", В вайтлисте: " + playersToWhitelist.size());
            
            // Send success message
            String successMsg = plugin.getConfig().getString("success-message",
                "<green>Успешно кикнуто <count> игрок(ов) без прав.</green>");
            successMsg = successMsg.replace("<count>", String.valueOf(playersToKick.size()));
            sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(successMsg)));
            
            return true;
        } else if (subCommand.equals("cancel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cЭту команду могут использовать только игроки.");
                return true;
            }
            
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            // Remove pending confirmation
            pendingConfirmations.remove(playerId);
            
            plugin.getLogger().info("[KICK] Игрок " + player.getName() + " отменил операцию кика");
            
            // Send cancel message
            String cancelMsg = plugin.getConfig().getString("cancel-message",
                "<red>Операция кика отменена.</red>");
            player.sendMessage(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(cancelMsg)));
            
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
            plugin.getLogger().info("[WHITELIST] Добавлен игрок " + player.getName() + " (" + player.getUniqueId() + ") в вайтлист (есть право kwlk.bypass)");
        } else {
            plugin.getLogger().info("[WHITELIST] Игрок " + player.getName() + " уже в вайтлисте");
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
