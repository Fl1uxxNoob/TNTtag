package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class TNTTagExplosionListener implements Listener {
    private TNTTagManager manager;

    public TNTTagExplosionListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if ((event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)
                && manager.isExplosionInProgress()) {
            event.setCancelled(true);
        }
    }
}