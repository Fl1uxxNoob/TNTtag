package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TNTTagMovementListener implements Listener {
    private TNTTagManager manager;

    public TNTTagMovementListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Blocca il movimento se il countdown è attivo
        if (manager.isCountdownActive()) {
            // Blocca il movimento orizzontale
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }
}