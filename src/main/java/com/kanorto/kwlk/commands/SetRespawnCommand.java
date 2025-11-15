package com.kanorto.kwlk.commands;

import com.kanorto.kwlk.KWLKPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to set the item in hand as a respawn item
 */
public class SetRespawnCommand implements CommandExecutor {
    
    private final KWLKPlugin plugin;
    private final MiniMessage miniMessage;
    
    public SetRespawnCommand(KWLKPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
        
        // Check permission
        if (!sender.hasPermission("kwlk.setrespawn")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды.");
            return true;
        }
        
        // Only players can use this command
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду могут использовать только игроки.");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if player is holding an item
        if (item.getType().isAir()) {
            sender.sendMessage("§cВы должны держать предмет в руке!");
            return true;
        }
        
        // Check if respawn item feature is enabled
        if (!plugin.getConfig().getBoolean("respawn-item.enabled", true)) {
            sender.sendMessage("§cФункция предмета возрождения отключена в конфигурации!");
            return true;
        }
        
        // Get respawn item configuration
        String displayName = plugin.getConfig().getString("respawn-item.display-name",
            "<gold><bold>Тотем Возрождения</bold></gold>");
        List<String> loreConfig = plugin.getConfig().getStringList("respawn-item.lore");
        
        // Apply respawn item properties to the item in hand
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Set display name
            String formattedName = LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(displayName));
            meta.setDisplayName(formattedName);
            
            // Set lore
            if (!loreConfig.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String loreLine : loreConfig) {
                    lore.add(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(loreLine)));
                }
                meta.setLore(lore);
            }
            
            item.setItemMeta(meta);
            
            // Log to console
            plugin.getLogger().info("[SETRESPAWN] Игрок " + player.getName() + " настроил предмет " + 
                item.getType().name() + " как предмет возрождения");
            
            // Send success message
            sender.sendMessage("§aПредмет в вашей руке теперь является предметом возрождения!");
            sender.sendMessage("§7Используйте его правой кнопкой мыши для воскрешения призраков.");
        } else {
            sender.sendMessage("§cНе удалось изменить предмет!");
            plugin.getLogger().warning("[SETRESPAWN] Не удалось получить ItemMeta для предмета " + item.getType().name());
        }
        
        return true;
    }
}
