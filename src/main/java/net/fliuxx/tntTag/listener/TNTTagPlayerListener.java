package net.fliuxx.tntTag.listener;

import net.fliuxx.tntTag.manager.TNTTagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class TNTTagPlayerListener implements Listener {
    private TNTTagManager manager;

    public TNTTagPlayerListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        // Rimuove la scoreboard e gestisce la rimozione dal gioco
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (manager.isGameActive()) {
            manager.removePlayer(p);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (manager.isGameActive()) {
            manager.removePlayer(p);
        }
    }
}