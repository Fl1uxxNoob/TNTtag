package net.fliuxx.tntTag.gui;

import net.fliuxx.tntTag.manager.TNTTagManager;
import net.fliuxx.tntTag.TntTag;
import net.fliuxx.tntTag.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ArenaSelectionGUI implements Listener {
    private TNTTagManager manager;
    private Inventory gui;
    private static ArenaSelectionGUI instance = null;
    private List<Arena> arenas = new ArrayList<Arena>();
    private int currentPage = 0;
    private final int ARENAS_PER_PAGE = 6; // slots 1-6

    private ArenaSelectionGUI(TNTTagManager manager) {
        this.manager = manager;
        loadArenas();
        createGUI();
    }

    public static ArenaSelectionGUI getInstance(TNTTagManager manager) {
        if (instance == null) {
            instance = new ArenaSelectionGUI(manager);
            TntTag.getInstance().getServer().getPluginManager().registerEvents(instance, TntTag.getInstance());
        } else {
            instance.loadArenas();
            instance.currentPage = 0;
            instance.updateItems();
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        instance = null;
    }

    private void loadArenas() {
        arenas.clear();
        FileConfiguration config = TntTag.getInstance().getConfig();
        if (config.isConfigurationSection("arenas")) {
            for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
                String world = config.getString("arenas." + key + ".world");
                double x = config.getDouble("arenas." + key + ".x");
                double y = config.getDouble("arenas." + key + ".y");
                double z = config.getDouble("arenas." + key + ".z");
                Arena arena = new Arena(key, new org.bukkit.Location(Bukkit.getWorld(world), x, y, z));
                arenas.add(arena);
            }
        }
    }

    private void createGUI() {
        gui = Bukkit.createInventory(null, 9, ChatColor.DARK_BLUE + "Seleziona Arena");
        updateItems();
    }

    public void updateItems() {
        // Slot 0: Opzione "Casuale"
        ItemStack randomItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = randomItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Casuale");
        // Se selectedArena è null, mostra l'enchant per indicare la selezione
        if (manager.getSelectedArena() == null) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        randomItem.setItemMeta(meta);
        gui.setItem(0, randomItem);

        // Rimuovi eventuali item nei slot 1-6
        for (int i = 1; i <= 6; i++) {
            gui.setItem(i, null);
        }

        // Calcola l'indice di partenza per la pagina corrente
        int startIndex = currentPage * ARENAS_PER_PAGE;
        int slot = 1;
        for (int i = startIndex; i < arenas.size() && i < startIndex + ARENAS_PER_PAGE; i++) {
            Arena arena = arenas.get(i);
            ItemStack item = new ItemStack(Material.BEACON);
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + arena.getName());
            if (manager.getSelectedArena() != null &&
                    manager.getSelectedArena().getName().equals(arena.getName())) {
                im.addEnchant(Enchantment.DURABILITY, 1, true);
            }
            item.setItemMeta(im);
            gui.setItem(slot, item);
            slot++;
        }

        // Slot 7: Pulsante "Pagina precedente" se esiste una pagina precedente, altrimenti vuoto
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Pagina precedente");
            prev.setItemMeta(prevMeta);
            gui.setItem(7, prev);
        } else {
            gui.setItem(7, new ItemStack(Material.AIR));
        }

        // Slot 8: Pulsante "Pagina successiva" se esiste una pagina successiva, altrimenti vuoto
        if ((currentPage + 1) * ARENAS_PER_PAGE < arenas.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Pagina successiva");
            next.setItemMeta(nextMeta);
            gui.setItem(8, next);
        } else {
            gui.setItem(8, new ItemStack(Material.AIR));
        }
    }

    public void openGUI(Player player) {
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().equals(ChatColor.DARK_BLUE + "Seleziona Arena")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == 0) {
            // L'opzione "Casuale": non impostiamo nessuna arena (rimane null)
            manager.setSelectedArena(null);
            player.sendMessage(ChatColor.GREEN + "Hai selezionato l'opzione 'Casuale'.");
        } else if (slot >= 1 && slot <= 6) {
            int index = currentPage * ARENAS_PER_PAGE + (slot - 1);
            if (index < arenas.size()) {
                Arena arena = arenas.get(index);
                manager.setSelectedArena(arena);
                player.sendMessage(ChatColor.GREEN + "Hai selezionato l'arena: " + arena.getName());
            }
        } else if (slot == 7) {
            // Pulsante per pagina precedente
            if (currentPage > 0) {
                currentPage--;
            }
        } else if (slot == 8) {
            // Pulsante per pagina successiva
            if ((currentPage + 1) * ARENAS_PER_PAGE < arenas.size()) {
                currentPage++;
            }
        }
        updateItems();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().getTitle().equals(ChatColor.DARK_BLUE + "Seleziona Arena")) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        // Riapre la GUI principale con un piccolo delay per evitare conflitti con l'evento di chiusura
        Bukkit.getScheduler().runTaskLater(TntTag.getInstance(), new Runnable() {
            @Override
            public void run() {
                net.fliuxx.tntTag.gui.TNTTagGUI.getInstance(manager).openGUI(player);
            }
        }, 1L);
    }
}