package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TNTTagListener implements Listener {
    private TNTTagManager manager;

    public TNTTagListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Assicurati che entrambi i partecipanti siano giocatori
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            // Controlla che entrambi siano in partita e che il gioco sia attivo
            if (!shouldProcess(damager) || !shouldProcess(target)) return;

            // Se il giocatore che attacca è il portatore della TNT, passa la TNT al target
            if (manager.getCurrentHolder() != null && manager.getCurrentHolder().equals(damager)) {
                manager.passTNT(target);
            }
        }
    }

    private boolean shouldProcess(Player player) {
        return manager.isGameActive() && manager.getActivePlayers().contains(player);
    }
}