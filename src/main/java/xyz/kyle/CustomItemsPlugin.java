package xyz.kyle;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomItemsPlugin extends JavaPlugin {

    private static final String CUSTOM_ITEM_KEY = "custom_item";
    private FileConfiguration config;
    private List<CustomItem> items = new ArrayList<>();
    private NamespacedKey itemKey;

    @Override
    public void onEnable() {
        this.itemKey = new NamespacedKey(this, CUSTOM_ITEM_KEY);
        saveDefaultConfig();
        loadCustomItems();
        getLogger().info("Loaded " + items.size() + " custom items!");
    }

    private void loadCustomItems() {
        config = getConfig();
        Set<String> keys = config.getKeys(false);

        for (String key : keys) {
            String[] parts = config.getString(key).split(":");
            if (parts.length != 2) {
                getLogger().warning("Invalid item format for " + key + ", should be 'namespace:item'");
                continue;
            }

            String namespace = parts[0];
            String itemName = parts[1];
            items.add(new CustomItem(namespace, itemName));
            getLogger().info("Registered custom item: " + namespace + ":" + itemName);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("givecustomitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("customitems.give")) {
                player.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage("§6Usage: §e/givecustomitem <item_name>");
                player.sendMessage("§6Available items: §e" + String.join(", ", getItemNames()));
                return true;
            }

            String requestedItem = args[0];
            CustomItem item = getItemByName(requestedItem);

            if (item == null) {
                player.sendMessage("§cItem not found! Available items: §e" + String.join(", ", getItemNames()));
                return true;
            }

            ItemStack itemStack = createItemStack(item);
            player.getInventory().addItem(itemStack);
            player.sendMessage("§aYou received a §e" + item.getDisplayName() + "§a!");
            return true;
        }
        return false; // Handle other commands if necessary
    }

    private ItemStack createItemStack(CustomItem item) {
        // Use a placeholder material that will be replaced by the texture pack
        ItemStack itemStack = new ItemStack(Material.STICK);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            // Set display name and lore
            meta.setDisplayName("§r" + item.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Custom Item");
            lore.add("§8" + item.getUniqueItemId());
            meta.setLore(lore);

            // Set custom model data
            meta.setCustomModelData(getCustomModelData(item));

            // Mark as a custom item in persistent data
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, item.getUniqueItemId());

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private int getCustomModelData(CustomItem item) {
        // Return a unique custom model data value for each item
        switch (item.getUniqueItemId()) {
            case "xyz:item_name_1":
                return 10001;
            case "xyz:item_name_2":
                return 10002;
            default:
                return 10000; // Default model data
        }
    }

    private List<String> getItemNames() {
        List<String> names = new ArrayList<>();
        for (CustomItem item : items) {
            names.add(item.getUniqueItemId());
        }
        return names;
    }

    private CustomItem getItemByName(String name) {
        for (CustomItem item : items) {
            if (item.getUniqueItemId().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    // Inner class for CustomItem
    public static class CustomItem {
        private final String namespace;
        private final String itemName;

        public CustomItem(String namespace, String itemName) {
            this.namespace = namespace;
            this.itemName = itemName;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getItemName() {
            return itemName;
        }

        public String getDisplayName() {
            return namespace + "'s " + itemName.replace("_", " ");
        }

        public String getUniqueItemId() {
            return namespace + ":" + itemName;
        }
    }
}
