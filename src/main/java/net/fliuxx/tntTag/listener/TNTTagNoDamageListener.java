package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class TNTTagNoDamageListener implements Listener {
    private TNTTagManager manager;

    public TNTTagNoDamageListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            // Durante il countdown, annulla ogni danno
            if (manager.isCountdownActive()) {
                event.setCancelled(true);
                return;
            }
            // Se il giocatore è in game, annulla il danno (ma permette il knockback)
            if (manager.getActivePlayers().contains(p)) {
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (manager.getActivePlayers().contains(p)) {
                event.setCancelled(true);
                p.setFoodLevel(20);
            }
        }
    }
}
