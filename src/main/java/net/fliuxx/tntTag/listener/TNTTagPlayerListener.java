package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.entity.Player;

public class TNTTagPlayerListener implements Listener {
    private TNTTagManager manager;

    public TNTTagPlayerListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!manager.isGameActive()) return;
        Player p = event.getPlayer();
        manager.removePlayer(p);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!manager.isGameActive()) return;
        Player p = event.getPlayer();
        manager.removePlayer(p);
    }
}
