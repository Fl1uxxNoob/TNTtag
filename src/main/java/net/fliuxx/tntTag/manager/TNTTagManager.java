package net.fliuxx.tntTag.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.fliuxx.tntTag.scoreboard.TNTTagScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import net.fliuxx.tntTag.TntTag;
import net.fliuxx.tntTag.arena.Arena;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TNTTagManager {
    private Player currentHolder;
    private BukkitTask roundTask;
    private int roundTimeRemaining;
    private int roundDuration = 60; // Tempo di default per il round (modificabile via GUI)
    private boolean gameActive = false;
    private List<Player> activePlayers = new ArrayList<Player>();
    private final Random random = new Random();
    // Flag per indicare che l'esplosione TNTTag è in corso (per annullare i danni)
    private boolean explosionInProgress = false;
    private Arena selectedArena = null;
    private boolean countdownActive = false;
    private TNTTagScoreboardManager scoreboardManager;

    // Avvia la partita con un countdown di 10 secondi, se ci sono almeno 2 giocatori online
    public void startGame() {
        if (gameActive) {
            Bukkit.getLogger().info("Il gioco è già in corso.");
            return;
        }
        // Controlla che ci siano almeno 2 giocatori online (compatibilità 1.8.8)
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.size() < 2) {
            for (Player p : onlinePlayers) {
                p.sendMessage(ChatColor.RED + "Non ci sono abbastanza giocatori online per iniziare la partita.");
            }
            return;
        }
        internalStartGameCountdown();
    }

    // Countdown di 10 secondi prima di avviare la partita
    private void internalStartGameCountdown() {
        // Aggiorna la lista dei giocatori attivi prima di iniziare il countdown
        activePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        teleportAllPlayersToArena(); // Ora activePlayers contiene i giocatori online
        countdownActive = true;
        new BukkitRunnable() {
            int countdown = 10;
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                if (onlinePlayers.size() < 2) {
                    for (Player p : onlinePlayers) {
                        p.sendMessage(ChatColor.RED + "Non ci sono abbastanza giocatori online per avviare TNTTag.");
                    }
                    countdownActive = false;
                    cancel();
                    return;
                }
                for (Player p : onlinePlayers) {
                    // Compatibilità 1.8.8 - usa sendTitle con reflection o metodo alternativo
                    p.sendMessage(ChatColor.GOLD + "Avvio TNTTag in " + countdown + "...");
                }
                countdown--;
                if (countdown < 0) {
                    countdownActive = false;
                    cancel();
                    internalStartGame();
                }
            }
        }.runTaskTimer(TntTag.getInstance(), 0L, 20L);
    }

    // Teletrasporta tutti i giocatori attivi allo spawn dell'arena scelta
    private void teleportAllPlayersToArena() {
        Arena arena = getSelectedArena();
        for (Player p : activePlayers) {
            p.teleport(arena.getSpawn());
        }
    }

    // Metodo interno che avvia la partita dopo il countdown
    private void internalStartGame() {
        activePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (activePlayers.size() < 2) {
            Bukkit.getLogger().info("Non ci sono abbastanza giocatori online per iniziare la partita.");
            return;
        }
        gameActive = true;
        roundTimeRemaining = roundDuration;

        // Crea e assegna la scoreboard a tutti i giocatori attivi
        scoreboardManager = new TNTTagScoreboardManager();
        for (Player p : activePlayers) {
            p.setScoreboard(scoreboardManager.getScoreboard());
        }

        chooseRandomHolder();
        updatePlayerEffects();
        startRoundTimer();
    }

    // Getter e setter per selectedArena
    public Arena getSelectedArena() {
        if (selectedArena != null) {
            return selectedArena;
        }
        // Se non è stato scelto, carica una casuale dal config
        FileConfiguration config = TntTag.getInstance().getConfig();
        List<Arena> arenaList = new ArrayList<Arena>();
        if (config.isConfigurationSection("arenas")) {
            for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
                String world = config.getString("arenas." + key + ".world");
                double x = config.getDouble("arenas." + key + ".x");
                double y = config.getDouble("arenas." + key + ".y");
                double z = config.getDouble("arenas." + key + ".z");
                Location spawn = new Location(Bukkit.getWorld(world), x, y, z);
                arenaList.add(new Arena(key, spawn));
            }
        }
        if (!arenaList.isEmpty()) {
            return arenaList.get(random.nextInt(arenaList.size()));
        }
        // Fallback
        return new Arena("Default", Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    public void setSelectedArena(Arena arena) {
        this.selectedArena = arena;
    }

    public boolean isCountdownActive() {
        return countdownActive;
    }

    // Seleziona casualmente un giocatore tra quelli attivi come portatore della TNT
    private void chooseRandomHolder() {
        int index = random.nextInt(activePlayers.size());
        currentHolder = activePlayers.get(index);
        broadcast(ChatColor.GRAY + "Il giocatore " + ChatColor.RED + currentHolder.getName() + ChatColor.GRAY + " ha la TNT!");
        assignTNT(currentHolder);
    }

    // Assegna la TNT al giocatore: nello slot 0 e come casco, con effetto "enchanted"
    private void assignTNT(Player p) {
        ItemStack tntItem = new ItemStack(Material.TNT);
        ItemMeta meta = tntItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "TNTTag");
        meta.addEnchant(Enchantment.DURABILITY, 1, true); // effetto brillante
        tntItem.setItemMeta(meta);
        p.getInventory().setItem(0, tntItem);
        p.getInventory().setHelmet(tntItem.clone());
    }

    // Rimuove la TNT dal giocatore (slot 0 e casco) - CORRETTO
    private void removeTNT(Player p) {
        // Rimuovi dal slot 0
        ItemStack item = p.getInventory().getItem(0);
        if (isTNTTagItem(item)) {
            p.getInventory().setItem(0, null);
        }
        // Rimuovi dal casco - CORREZIONE: controllo e rimozione corretti
        ItemStack helmet = p.getInventory().getHelmet();
        if (isTNTTagItem(helmet)) {
            p.getInventory().setHelmet(null);
        }
    }

    private boolean isTNTTagItem(ItemStack item) {
        return item != null
                && item.getType() == Material.TNT
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && (ChatColor.RED + "TNTTag").equals(item.getItemMeta().getDisplayName());
    }

    // Aggiorna gli effetti di velocità: tutti i giocatori ottengono Speed I, mentre il portatore Speed II.
    public void updatePlayerEffects() {
        for (Player p : activePlayers) {
            p.removePotionEffect(PotionEffectType.SPEED);
            if (p.equals(currentHolder)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            }
        }
    }

    // Passa la TNT al giocatore target senza resettare il timer del round - CORRETTO
    public void passTNT(Player newHolder) {
        if (!gameActive) return;
        if (currentHolder != null && currentHolder.equals(newHolder)) return;
        if (!activePlayers.contains(newHolder)) return;

        broadcast(ChatColor.GRAY + "La TNT è passata da " + ChatColor.RED + currentHolder.getName() + ChatColor.GRAY + " a " + ChatColor.RED + newHolder.getName());

        // CORREZIONE: Prima rimuovi la TNT dal vecchio holder
        if (currentHolder != null) {
            removeTNT(currentHolder);
            currentHolder.getInventory().clear();
        }

        // Poi assegna al nuovo holder
        currentHolder = newHolder;
        assignTNT(currentHolder);
        updatePlayerEffects();
    }

    // Avvia il timer globale del round
    private void startRoundTimer() {
        roundTask = Bukkit.getScheduler().runTaskTimer(TntTag.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (roundTimeRemaining <= 0) {
                    eliminateCurrentHolder();
                } else {
                    // Aggiorna la scoreboard ad ogni tick - CORREZIONE: controllo null safety
                    if (scoreboardManager != null && gameActive) {
                        scoreboardManager.updateScoreboard(roundTimeRemaining, activePlayers.size());
                    }
                    if (currentHolder != null && currentHolder.isOnline()) {
                        currentHolder.sendMessage(ChatColor.GRAY + "Tempo rimanente per passare la TNT: " + ChatColor.RED + roundTimeRemaining + ChatColor.GRAY + " secondi");
                    }
                    roundTimeRemaining--;
                }
            }
        }, 20, 20);
    }

    // Elimina il giocatore portatore al termine del timer del round
    private void eliminateCurrentHolder() {
        if (currentHolder != null && currentHolder.isOnline()) {
            explosionInProgress = true;
            Location loc = currentHolder.getLocation();
            // Compatibilità 1.8.8: usa le coordinate invece di Location object
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4.0F);
            currentHolder.sendMessage(ChatColor.RED + "BOOM! La TNT è esplosa su di te!");
            broadcast(ChatColor.RED + "Il giocatore " + ChatColor.DARK_RED +  currentHolder.getName() + ChatColor.RED + " è stato eliminato!");
            // Compatibilità 1.8.8 - sendTitle non disponibile
            currentHolder.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "SEI MORTO");
            removeTNT(currentHolder);
            currentHolder.getInventory().clear();
            // CORREZIONE: Reset scoreboard per il giocatore eliminato
            currentHolder.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            activePlayers.remove(currentHolder);
            teleportToSpawn(currentHolder);
            Bukkit.getScheduler().runTaskLater(TntTag.getInstance(), new Runnable() {
                @Override
                public void run() {
                    explosionInProgress = false;
                }
            }, 1L);
        }
        cancelRoundTimer();
        if (activePlayers.size() > 1) {
            teleportAllPlayersToArena();
            broadcast(ChatColor.GRAY + "Nuovo round in partenza con " + ChatColor.YELLOW + activePlayers.size() + ChatColor.GRAY + " giocatori!");
            roundTimeRemaining = roundDuration;
            chooseRandomHolder();
            updatePlayerEffects();
            startRoundTimer();
        } else if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            broadcast(ChatColor.GREEN + "Il vincitore è " + ChatColor.YELLOW + winner.getName() + ChatColor.GREEN + "!");
            winner.getInventory().clear();
            // CORREZIONE: Reset scoreboard per il vincitore
            winner.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            teleportToSpawn(winner);
            // Compatibilità 1.8.8 - sendTitle non disponibile
            winner.sendMessage(ChatColor.YELLOW + "HAI VINTO!");
            stopGame();
        } else {
            broadcast(ChatColor.RED + "Nessun giocatore rimasto, partita terminata!");
            stopGame();
        }
    }

    // Cancella il timer del round
    public void cancelRoundTimer() {
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }
    }

    // Ferma il gioco: svuota l'inventario di tutti e teletrasporta allo spawn - CORRETTO
    public void stopGame() {
        if (!gameActive) return;
        cancelRoundTimer();
        broadcast(ChatColor.RED + "La partita di TNTTag è terminata!");

        // CORREZIONE: Reset completo per tutti i giocatori attivi
        for (Player p : new ArrayList<Player>(activePlayers)) {
            p.getInventory().clear();
            p.removePotionEffect(PotionEffectType.SPEED);
            // CORREZIONE: Assicurati che la scoreboard venga resettata
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            teleportToSpawn(p);
        }

        // Per sicurezza, rimuovo anche da eventuali altri giocatori in game (opzionale)
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.SPEED);
        }

        gameActive = false;
        activePlayers.clear();
        currentHolder = null;
        // CORREZIONE: Reset del scoreboardManager
        scoreboardManager = null;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public List<Player> getActivePlayers() {
        return activePlayers;
    }

    public Player getCurrentHolder() {
        return currentHolder;
    }

    public boolean isExplosionInProgress() {
        return explosionInProgress;
    }

    // Gestisce la rimozione di un giocatore (per uscita o cambio mondo) - CORRETTO
    public void removePlayer(Player p) {
        if (activePlayers.contains(p)) {
            activePlayers.remove(p);
            // CORREZIONE: Reset scoreboard quando il giocatore viene rimosso
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            if (p.equals(currentHolder)) {
                removeTNT(p);
                broadcast(ChatColor.GRAY + "Il giocatore " + ChatColor.DARK_RED + p.getName() + ChatColor.RED + " è uscito ed era il portatore della TNT.");
                cancelRoundTimer();
                currentHolder = null; // CORREZIONE: Reset currentHolder

                if (!activePlayers.isEmpty()) {
                    if (activePlayers.size() == 1) {
                        Player winner = activePlayers.get(0);
                        broadcast(ChatColor.GREEN + "Il vincitore è " + ChatColor.YELLOW +  winner.getName() + ChatColor.GREEN + "!");
                        winner.getInventory().clear();
                        winner.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        teleportToSpawn(winner);
                        // Compatibilità 1.8.8 - sendTitle non disponibile
                        winner.sendMessage(ChatColor.YELLOW + "HAI VINTO!");
                        stopGame();
                        return;
                    } else {
                        broadcast(ChatColor.GRAY + "Nuovo round in partenza con " + ChatColor.YELLOW + activePlayers.size() + ChatColor.GRAY + " giocatori!");
                        roundTimeRemaining = roundDuration;
                        chooseRandomHolder();
                        startRoundTimer();
                    }
                } else {
                    broadcast(ChatColor.RED + "Nessun giocatore rimasto, partita terminata!");
                    stopGame();
                }
            }
            if (activePlayers.size() == 1 && gameActive) {
                Player winner = activePlayers.get(0);
                broadcast(ChatColor.GREEN + "Il vincitore è " + ChatColor.YELLOW +  winner.getName() + ChatColor.GREEN + "!");
                winner.getInventory().clear();
                winner.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                teleportToSpawn(winner);
                // Compatibilità 1.8.8 - sendTitle non disponibile
                winner.sendMessage(ChatColor.YELLOW + "HAI VINTO!");
                stopGame();
            }
        }
    }

    // Teletrasporta un giocatore allo spawn configurato in config.yml
    private void teleportToSpawn(Player p) {
        FileConfiguration config = TntTag.getInstance().getConfig();
        String worldName = config.getString("spawn.world", p.getWorld().getName());
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = p.getWorld();
        }
        double x = config.getDouble("spawn.x", 0.5);
        double y = config.getDouble("spawn.y", 65);
        double z = config.getDouble("spawn.z", 0.5);
        Location spawn = new Location(world, x, y, z);
        p.teleport(spawn);
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage("[TNTTag] " + message);
    }

    // Getter e setter per il tempo di round
    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int duration) {
        this.roundDuration = duration;
    }
}