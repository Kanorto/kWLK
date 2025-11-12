package com.kanorto.kwlk.commands;

import com.kanorto.kwlk.KWLKPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command to give respawn items to players
 */
public class GiveRespawnCommand implements CommandExecutor, TabCompleter {
    
    private final KWLKPlugin plugin;
    private final MiniMessage miniMessage;
    
    public GiveRespawnCommand(KWLKPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
        
        // Check permission
        if (!sender.hasPermission("kwlk.giverespawn")) {
            sender.sendMessage(Component.text("§cУ вас нет прав для использования этой команды."));
            return true;
        }
        
        // Check if respawn item feature is enabled
        if (!plugin.getConfig().getBoolean("respawn-item.enabled", true)) {
            sender.sendMessage(Component.text("§cФункция предмета возрождения отключена в конфигурации!"));
            return true;
        }
        
        // Check arguments
        if (args.length < 1) {
            sender.sendMessage(Component.text("§cИспользование: /giverespawn <игрок> [количество]"));
            return true;
        }
        
        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cИгрок не найден: " + args[0]));
            return true;
        }
        
        // Get amount (default 1)
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(Component.text("§cКоличество должно быть от 1 до 64!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cНеверное количество: " + args[1]));
                return true;
            }
        }
        
        // Create respawn item
        ItemStack respawnItem = createRespawnItem(amount);
        if (respawnItem == null) {
            sender.sendMessage(Component.text("§cНе удалось создать предмет возрождения. Проверьте конфигурацию!"));
            return true;
        }
        
        // Give item to player
        target.getInventory().addItem(respawnItem);
        
        // Log to console
        plugin.getLogger().info("[GIVERESPAWN] " + sender.getName() + " выдал " + amount + " предметов возрождения игроку " + target.getName());
        
        // Send messages
        sender.sendMessage(Component.text("§aВыдано " + amount + " предмет(ов) возрождения игроку " + target.getName()));
        target.sendMessage(Component.text("§aВы получили " + amount + " предмет(ов) возрождения!"));
        
        return true;
    }
    
    /**
     * Create a respawn item based on config
     */
    private ItemStack createRespawnItem(int amount) {
        String materialName = plugin.getConfig().getString("respawn-item.material", "TOTEM_OF_UNDYING");
        Material material;
        
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid respawn item material in config: " + materialName);
            return null;
        }
        
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            String displayName = plugin.getConfig().getString("respawn-item.display-name",
                "<gold><bold>Totem of Revival</bold></gold>");
            Component nameComponent = miniMessage.deserialize(displayName);
            meta.displayName(nameComponent);
            
            // Set lore
            List<String> loreConfig = plugin.getConfig().getStringList("respawn-item.lore");
            if (!loreConfig.isEmpty()) {
                List<Component> lore = new ArrayList<>();
                for (String loreLine : loreConfig) {
                    lore.add(miniMessage.deserialize(loreLine));
                }
                meta.lore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // Suggest online player names
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        } else if (args.length == 2) {
            // Suggest amounts
            List<String> amounts = new ArrayList<>();
            amounts.add("1");
            amounts.add("5");
            amounts.add("10");
            return amounts;
        }
        return Collections.emptyList();
    }
}
