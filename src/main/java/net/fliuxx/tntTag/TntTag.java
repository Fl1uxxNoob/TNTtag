package net.fliuxx.tntTag;

import net.fliuxx.tntTag.worldguard.ArenaWorldGuardManager;
import org.bukkit.plugin.java.JavaPlugin;
import net.fliuxx.tntTag.listener.*;
import net.fliuxx.tntTag.manager.TNTTagManager;
import net.fliuxx.tntTag.command.TNTTagCommand;

public class TntTag extends JavaPlugin {
    private static TntTag instance;
    private TNTTagManager manager;

    public static TntTag getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Inizializza il manager di gioco (ora con tempo variabile)
        manager = new TNTTagManager();

        // Applica le impostazioni per le arene via WorldGuard e gamerule
        ArenaWorldGuardManager.checkAndApplySettings();

        // Registra i listener (passando il manager dove necessario)
        getServer().getPluginManager().registerEvents(new TNTTagListener(manager), this);
        getServer().getPluginManager().registerEvents(new TNTTagPlayerListener(manager), this);
        getServer().getPluginManager().registerEvents(new TNTTagItemListener(manager), this);
        getServer().getPluginManager().registerEvents(new TNTTagExplosionListener(manager), this);
        getServer().getPluginManager().registerEvents(new TNTTagMovementListener(manager), this);
        getServer().getPluginManager().registerEvents(new TNTTagNoDamageListener(manager), this);

        // Registra il comando /tnttag (ora gestisce anche "gui")
        getCommand("tnttag").setExecutor(new TNTTagCommand(manager));

        getLogger().info("TntTag plugin abilitato!");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.stopGame();
        }
        getLogger().info("TntTag plugin disabilitato!");
    }

    public TNTTagManager getManager() {
        return manager;
    }
}