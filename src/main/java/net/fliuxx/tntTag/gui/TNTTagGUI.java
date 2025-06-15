package net.fliuxx.tntTag.gui;

import net.fliuxx.tntTag.manager.MessageManager;
import net.fliuxx.tntTag.manager.TNTTagManager;
import net.fliuxx.tntTag.TntTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TNTTagGUI implements Listener {
    private static TNTTagGUI instance = null;
    private TNTTagManager manager;
    private MessageManager messageManager;
    private Inventory gui;

    private TNTTagGUI(TNTTagManager manager) {
        this.manager = manager;
        this.messageManager = MessageManager.getInstance();
        createGUI();
    }

    public static TNTTagGUI getInstance(TNTTagManager manager) {
        if (instance == null) {
            instance = new TNTTagGUI(manager);
            TntTag.getInstance().getServer().getPluginManager().registerEvents(instance, TntTag.getInstance());
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        instance = null;
    }

    private void createGUI() {
        gui = Bukkit.createInventory(null, 9, messageManager.getMainGUITitle());
        updateItems();
    }

    private void updateItems() {
        // Pulsante per modificare il tempo
        ItemStack timeButton = new ItemStack(Material.WATCH);
        ItemMeta timeMeta = timeButton.getItemMeta();
        timeMeta.setDisplayName(messageManager.getTimeButtonName());
        timeMeta.setLore(messageManager.getTimeButtonLore(manager.getRoundDuration()));
        timeButton.setItemMeta(timeMeta);
        gui.setItem(2, timeButton);

        // Pulsante per avviare la partita
        ItemStack startButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta startMeta = startButton.getItemMeta();
        startMeta.setDisplayName(messageManager.getStartButtonName());
        startMeta.setLore(messageManager.getStartButtonLore());
        startButton.setItemMeta(startMeta);
        gui.setItem(4, startButton);

        // Pulsante per selezionare l'arena
        ItemStack arenaButton = new ItemStack(Material.COMPASS);
        ItemMeta arenaMeta = arenaButton.getItemMeta();
        arenaMeta.setDisplayName(messageManager.getArenaButtonName());
        arenaMeta.setLore(messageManager.getArenaButtonLore());
        arenaButton.setItemMeta(arenaMeta);
        gui.setItem(6, arenaButton);
    }

    public void openGUI(Player player) {
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().equals(messageManager.getMainGUITitle())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission("tnttag.host") || manager.isGameActive()) {
            player.sendMessage(messageManager.getCannotUseNowMessage());
            return;
        }

        int slot = event.getRawSlot();
        if (slot == 2) {
            // Gestione modifica tempo
            if (event.isLeftClick()) {
                manager.setRoundDuration(manager.getRoundDuration() + 10);
                player.sendMessage(messageManager.getTimeIncreasedMessage(manager.getRoundDuration()));
            } else if (event.isRightClick()) {
                int newTime = manager.getRoundDuration() - 10;
                if (newTime < 10) newTime = 10;
                manager.setRoundDuration(newTime);
                player.sendMessage(messageManager.getTimeDecreasedMessage(manager.getRoundDuration()));
            }
            updateItems();
        } else if (slot == 4) {
            // Avvia partita
            manager.startGame();
            player.closeInventory();
        } else if (slot == 6) {
            // Apri selezione arena
            ArenaSelectionGUI.getInstance(manager).openGUI(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Non facciamo nulla alla chiusura
    }
}