package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class TNTTagItemListener implements Listener {
    private TNTTagManager manager;

    public TNTTagItemListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!shouldProcess(player)) return;
        ItemStack item = event.getItemDrop().getItemStack();
        if (isTNTTagItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldProcess(player)) return;
        ItemStack currentItem = event.getCurrentItem();
        if (isTNTTagItem(currentItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!shouldProcess(player)) return;
        ItemStack item = event.getItemInHand();
        if (isTNTTagItem(item)) {
            event.setCancelled(true);
        }
    }

    private boolean isTNTTagItem(ItemStack item) {
        return item != null
                && item.getType() == Material.TNT
                && item.hasItemMeta()
                && "TNTTag".equals(item.getItemMeta().getDisplayName());
    }

    // Restituisce true se la TNTTag è attiva e il giocatore è in gioco
    private boolean shouldProcess(Player player) {
        return manager.isGameActive() && manager.getActivePlayers().contains(player);
    }
}