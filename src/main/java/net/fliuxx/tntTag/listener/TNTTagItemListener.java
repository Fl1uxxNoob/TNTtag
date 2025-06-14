package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

        // Controlla l'item corrente (quello su cui si clicca)
        ItemStack currentItem = event.getCurrentItem();
        if (isTNTTagItem(currentItem)) {
            event.setCancelled(true);
            return;
        }

        // Controlla l'item nel cursore (quello che si sta trascinando)
        ItemStack cursorItem = event.getCursor();
        if (isTNTTagItem(cursorItem)) {
            event.setCancelled(true);
            return;
        }

        // Controlla l'item nell'hotbar se si usa il tasto numerico
        if (event.getHotbarButton() != -1) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (isTNTTagItem(hotbarItem)) {
                event.setCancelled(true);
                return;
            }
        }

        // Controlla specificamente lo slot del casco (slot 39 nell'inventario del giocatore)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getRawSlot() == 5) {
            // Slot 5 corrisponde al casco nell'inventario del giocatore
            event.setCancelled(true);
            return;
        }

        // Controlla anche il drag and drop verso lo slot del casco
        if (event.getClickedInventory() != null &&
                event.getClickedInventory().equals(player.getInventory()) &&
                event.getSlot() == 39) { // Slot 39 = casco
            if (isTNTTagItem(cursorItem)) {
                event.setCancelled(true);
                return;
            }
        }

        // Previeni qualsiasi spostamento della TNT tramite shift+click
        if (event.isShiftClick()) {
            if (isTNTTagItem(currentItem)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!shouldProcess(player)) return;

        // Controlla l'item che si sta trascinando
        ItemStack draggedItem = event.getOldCursor();
        if (isTNTTagItem(draggedItem)) {
            event.setCancelled(true);
            return;
        }

        // Controlla se si sta trascinando verso il slot del casco
        if (event.getRawSlots().contains(5)) { // Slot 5 = casco nell'inventario GUI
            event.setCancelled(true);
            return;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!shouldProcess(player)) return;
        ItemStack item = event.getItem();
        if (isTNTTagItem(item)) {
            // Previeni l'uso dell'item TNT (click destro per posizionare, ecc.)
            event.setCancelled(true);
        }
    }

    private boolean isTNTTagItem(ItemStack item) {
        return item != null
                && item.getType() == Material.TNT
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && "TNTTag".equals(item.getItemMeta().getDisplayName());
    }

    // Restituisce true se la TNTTag è attiva e il giocatore è in gioco
    private boolean shouldProcess(Player player) {
        return manager.isGameActive() && manager.getActivePlayers().contains(player);
    }
}