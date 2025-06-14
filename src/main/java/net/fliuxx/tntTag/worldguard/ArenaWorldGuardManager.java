package net.fliuxx.tntTag.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.fliuxx.tntTag.TntTag;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class ArenaWorldGuardManager {

    private static WorldGuardPlugin worldGuard;

    // Inizializza WorldGuard
    private static WorldGuardPlugin getWorldGuard() {
        if (worldGuard == null) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
            if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
                Bukkit.getLogger().severe("[TntTag] WorldGuard non trovato!");
                return null;
            }
            worldGuard = (WorldGuardPlugin) plugin;
        }
        return worldGuard;
    }

    // Metodo principale: per ogni arena (definita in config.yml) si controlla la regione globale e si applicano i flag
    public static void checkAndApplySettings() {
        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) {
            Bukkit.getLogger().severe("[TntTag] Impossibile accedere a WorldGuard!");
            return;
        }

        FileConfiguration config = TntTag.getInstance().getConfig();
        if (!config.isConfigurationSection("arenas")) {
            Bukkit.getLogger().info("[TntTag] Nessuna arena definita nel config.yml.");
            return;
        }

        // Raccogli i mondi in cui sono definite le arene, evitando duplicati
        Set<World> arenaWorlds = new HashSet<>();
        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            String worldName = config.getString("arenas." + key + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                arenaWorlds.add(world);
                Bukkit.getLogger().info("[TntTag] Arena '" + key + "' trovata nel mondo: " + worldName);
            } else {
                Bukkit.getLogger().warning("[TntTag] Mondo '" + worldName + "' per arena '" + key + "' non trovato!");
            }
        }

        // Per ogni mondo, controlla la regione __global__ e applica i flag
        for (World world : arenaWorlds) {
            try {
                Bukkit.getLogger().info("[TntTag] Applicando impostazioni per il mondo: " + world.getName());

                RegionManager regionManager = wg.getRegionManager(world);
                if (regionManager == null) {
                    Bukkit.getLogger().warning("[TntTag] Impossibile ottenere RegionManager per il mondo: " + world.getName());
                    continue;
                }

                ProtectedRegion global = regionManager.getRegion("__global__");
                if (global == null) {
                    Bukkit.getLogger().warning("[TntTag] Regione __global__ non trovata nel mondo: " + world.getName());
                    // Proviamo a crearla se non esiste (questo potrebbe non funzionare su tutti i server)
                    continue;
                }

                // Imposta i flag utilizzando l'API di WorldGuard 6.x per 1.8.8
                Bukkit.getLogger().info("[TntTag] Applicando flag WorldGuard per " + world.getName());

                global.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.BLOCK_PLACE, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.INTERACT, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.SLEEP, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.ITEM_DROP, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.ITEM_PICKUP, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.EXP_DROPS, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.LAVA_FIRE, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
                global.setFlag(DefaultFlag.FALL_DAMAGE, StateFlag.State.DENY);

                // Imposta PvP su ALLOW per consentire il combattimento
                global.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);

                // Forza il salvataggio delle modifiche alla regione
                try {
                    regionManager.save();
                    Bukkit.getLogger().info("[TntTag] Flag WorldGuard salvati per il mondo " + world.getName());
                } catch (Exception saveEx) {
                    Bukkit.getLogger().warning("[TntTag] Errore nel salvataggio dei flag per " + world.getName() + ": " + saveEx.getMessage());
                }

            } catch (Exception e) {
                Bukkit.getLogger().severe("[TntTag] Errore nell'applicazione dei flag WorldGuard per il mondo " + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }

            // Applica le gamerule specifiche per Minecraft 1.8.8
            try {
                Bukkit.getLogger().info("[TntTag] Applicando gamerule per " + world.getName());

                // Per 1.8.8, le gamerule non supportano il parametro mondo nel comando
                // Dobbiamo impostarle direttamente sul mondo
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("doMobSpawning", "false");
                world.setGameRuleValue("keepInventory", "false");
                world.setGameRuleValue("doFireTick", "false");

                // Imposta la difficoltà su peaceful per il mondo
                world.setDifficulty(Difficulty.PEACEFUL);

                Bukkit.getLogger().info("[TntTag] Gamerule applicate con successo per il mondo " + world.getName());

            } catch (Exception e) {
                Bukkit.getLogger().warning("[TntTag] Errore nell'applicazione delle gamerule per il mondo " + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        Bukkit.getLogger().info("[TntTag] Configurazione arene completata per " + arenaWorlds.size() + " mondi.");
    }
}