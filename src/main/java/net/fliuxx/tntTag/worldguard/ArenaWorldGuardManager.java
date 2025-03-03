package net.fliuxx.tntTag.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.fliuxx.tntTag.TntTag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class ArenaWorldGuardManager {

    // I nomi dei flag da impostare (tutti su DENY)
    private static final String[] FLAG_NAMES = {
            "build",
            "block-place",
            "block-break",
            "use",
            "interact",
            "sleep",
            "chest-access",
            "use-anvil",
            "enderman-grief",
            "item-drop",
            "item-pickup",
            "exp-drops",
            "item-frame-rotation",
            "mob-damage",
            "lava-fire",
            "mob-spawning",
            "fall-damage"
    };

    // Metodo principale: per ogni arena (definita in config.yml) si controlla la regione globale e si applicano i flag
    public static void checkAndApplySettings() {
        FileConfiguration config = TntTag.getInstance().getConfig();
        if (!config.isConfigurationSection("arenas")) {
            Bukkit.getLogger().info("[TntTag] Nessuna arena definita nel config.yml.");
            return;
        }
        // Prepara il flag registry
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        // Registra i flag della lista, se non esistono
        for (String flagName : FLAG_NAMES) {
            Flag<?> flag = null;
            try {
                flag = registry.get(flagName);
            } catch (Exception e) {
                // ignora
            }
            if (flag == null) {
                StateFlag newFlag = new StateFlag(flagName, true);
                try {
                    registry.register(newFlag);
                    Bukkit.getLogger().info("[TntTag] Registrato flag personalizzato: " + flagName);
                } catch (FlagConflictException e) {
                    Bukkit.getLogger().severe("[TntTag] Conflitto nella registrazione del flag: " + flagName);
                }
            }
        }
        // Gestione della flag "pvp": se non è registrata, la registriamo
        Flag<?> pvpFlagGeneric = null;
        try {
            pvpFlagGeneric = registry.get("pvp");
        } catch (Exception e) {
            // ignora
        }
        if (pvpFlagGeneric == null) {
            StateFlag pvpFlag = new StateFlag("pvp", true);
            try {
                registry.register(pvpFlag);
                Bukkit.getLogger().info("[TntTag] Registrato flag personalizzato: pvp");
            } catch (FlagConflictException e) {
                Bukkit.getLogger().severe("[TntTag] Conflitto nella registrazione del flag: pvp");
            }
        }

        // Raccogli i mondi in cui sono definite le arene, evitando duplicati
        Set<World> arenaWorlds = new HashSet<>();
        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            String worldName = config.getString("arenas." + key + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                arenaWorlds.add(world);
            }
        }
        // Per ogni mondo, controlla la regione __global__ e applica i flag
        for (World world : arenaWorlds) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager == null) continue;
            ProtectedRegion global = regionManager.getRegion("__global__");
            if (global == null) continue;
            // Imposta tutti i flag della lista su DENY
            for (String flagName : FLAG_NAMES) {
                @SuppressWarnings("unchecked")
                Flag<StateFlag.State> flag = (Flag<StateFlag.State>) registry.get(flagName);
                if (flag != null) {
                    global.setFlag(flag, StateFlag.State.DENY);
                }
            }
            // Imposta la flag "pvp" su ALLOW (ovvero true, per consentire il PvP)
            @SuppressWarnings("unchecked")
            Flag<StateFlag.State> pvpFlag = (Flag<StateFlag.State>) registry.get("pvp");
            if (pvpFlag != null) {
                global.setFlag(pvpFlag, StateFlag.State.ALLOW);
            }
            // Salva le modifiche nella regione
            try {
                regionManager.save();
                Bukkit.getLogger().info("[TntTag] Aggiornati i flag global per il mondo " + world.getName());
            } catch (Exception e) {
                Bukkit.getLogger().severe("[TntTag] Errore nel salvataggio dei flag per il mondo " + world.getName());
            }
            // Esegui i comandi per impostare le gamerule
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv gamerule doDaylightCycle false " + world.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv gamerule doWeatherCycle false " + world.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv gamerule doMobSpawning false " + world.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvm set difficulty peaceful " + world.getName());
        }
    }
}
