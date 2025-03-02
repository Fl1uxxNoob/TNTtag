package net.fliuxx.tntTag.gui;

import net.fliuxx.tntTag.manager.TNTTagManager;
import net.fliuxx.tntTag.TntTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class TNTTagGUI implements Listener {
    private static TNTTagGUI instance = null;
    private TNTTagManager manager;
    private Inventory gui;

    private TNTTagGUI(TNTTagManager manager) {
        this.manager = manager;
        createGUI();
    }

    public static TNTTagGUI getInstance(TNTTagManager manager) {
        if (instance == null) {
            instance = new TNTTagGUI(manager);
            TntTag.getInstance().getServer().getPluginManager().registerEvents(instance, TntTag.getInstance());
        }
        return instance;
    }

    private void createGUI() {
        gui = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "TNTTag GUI");
        updateItems();
    }

    private void updateItems() {
        // Bottone per modificare il tempo del round (slot 3)
        ItemStack timeButton = new ItemStack(Material.CLOCK);
        ItemMeta timeMeta = timeButton.getItemMeta();
        timeMeta.setDisplayName(ChatColor.AQUA + "Modifica tempo round");
        int currentTime = manager.getRoundDuration();
        timeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Tempo attuale: " + currentTime + " sec",
                ChatColor.GRAY + "Sinistro: +10 sec, Destro: -10 sec"));
        timeButton.setItemMeta(timeMeta);
        gui.setItem(3, timeButton);

        // Bottone per avviare la TNTTag (slot 5)
        ItemStack startButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta startMeta = startButton.getItemMeta();
        startMeta.setDisplayName(ChatColor.GREEN + "Avvia TNTTag");
        startMeta.setLore(Arrays.asList(ChatColor.GRAY + "Clic sinistro per avviare"));
        startButton.setItemMeta(startMeta);
        gui.setItem(5, startButton);
    }

    public void openGUI(Player player) {
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_RED + "TNTTag GUI")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("tnttag.host") || manager.isGameActive()) {
            player.sendMessage(ChatColor.RED + "Non puoi usare questa GUI ora.");
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 3) {
            if (event.isLeftClick()) {
                manager.setRoundDuration(manager.getRoundDuration() + 10);
                player.sendMessage(ChatColor.GREEN + "Tempo del round aumentato a " + manager.getRoundDuration() + " sec.");
            } else if (event.isRightClick()) {
                int newTime = manager.getRoundDuration() - 10;
                if (newTime < 10) {
                    newTime = 10;
                }
                manager.setRoundDuration(newTime);
                player.sendMessage(ChatColor.GREEN + "Tempo del round diminuito a " + manager.getRoundDuration() + " sec.");
            }
            updateItems();
            player.updateInventory();
        } else if (slot == 5) {
            manager.startGame();
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Eventuale gestione alla chiusura della GUI
    }
}
