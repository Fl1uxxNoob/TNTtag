package net.fliuxx.tntTag.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class TNTTagManager {
    private Player currentHolder;
    private BukkitTask roundTask;
    private int roundTimeRemaining;
    private int roundDuration = 30; // Tempo di default per il round (modificabile via GUI)
    private boolean gameActive = false;
    private List<Player> activePlayers = new ArrayList<>();
    private final Random random = new Random();
    // Flag per indicare che l'esplosione TNTTag è in corso (per annullare i danni)
    private boolean explosionInProgress = false;

    // Avvia la partita con un countdown di 10 secondi, se ci sono almeno 2 giocatori online
    public void startGame() {
        if (gameActive) {
            Bukkit.getLogger().info("Il gioco è già in corso.");
            return;
        }
        // Controlla che ci siano almeno 2 giocatori online
        if (Bukkit.getOnlinePlayers().size() < 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.RED + "Non ci sono abbastanza giocatori online per iniziare la partita.");
            }
            return;
        }
        internalStartGameCountdown();
    }

    // Countdown di 10 secondi prima di avviare la partita
    private void internalStartGameCountdown() {
        new BukkitRunnable() {
            int countdown = 10;
            @Override
            public void run() {
                // Se durante il countdown il numero di giocatori scende sotto 2, annulla il countdown
                if (Bukkit.getOnlinePlayers().size() < 2) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.RED + "Non ci sono abbastanza giocatori online per avviare TNTTag.");
                    }
                    cancel();
                    return;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.GOLD + "Avvio TNTTag in " + countdown + "...", "", 10, 20, 10);
                }
                countdown--;
                if (countdown < 0) {
                    cancel();
                    internalStartGame();
                }
            }
        }.runTaskTimer(TntTag.getInstance(), 0L, 20L);
    }

    // Metodo interno che avvia la partita dopo il countdown
    private void internalStartGame() {
        activePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        // Verifica finale per evitare di avviare la partita con meno di 2 giocatori
        if (activePlayers.size() < 2) {
            Bukkit.getLogger().info("Non ci sono abbastanza giocatori online per iniziare la partita.");
            return;
        }
        gameActive = true;
        roundTimeRemaining = roundDuration;
        chooseRandomHolder();
        startRoundTimer();
    }

    // Seleziona casualmente un giocatore tra quelli attivi come portatore della TNT
    private void chooseRandomHolder() {
        int index = random.nextInt(activePlayers.size());
        currentHolder = activePlayers.get(index);
        broadcast("Il giocatore " + currentHolder.getName() + " ha la TNT!");
        assignTNT(currentHolder);
    }

    // Assegna la TNT al giocatore: nello slot 0 e come casco, con effetto "enchanted"
    private void assignTNT(Player p) {
        ItemStack tntItem = new ItemStack(Material.TNT);
        ItemMeta meta = tntItem.getItemMeta();
        meta.setDisplayName("TNTTag");
        meta.addEnchant(Enchantment.UNBREAKING, 1, true); // effetto brillante
        tntItem.setItemMeta(meta);
        p.getInventory().setItem(0, tntItem);
        p.getInventory().setHelmet(tntItem.clone());
    }

    // Rimuove la TNT dal giocatore (slot 0 e casco)
    private void removeTNT(Player p) {
        ItemStack item = p.getInventory().getItem(0);
        if (isTNTTagItem(item)) {
            p.getInventory().setItem(0, null);
        }
        ItemStack helmet = p.getInventory().getHelmet();
        if (isTNTTagItem(helmet)) {
            p.getInventory().setHelmet(null);
        }
    }

    private boolean isTNTTagItem(ItemStack item) {
        return item != null
                && item.getType() == Material.TNT
                && item.hasItemMeta()
                && "TNTTag".equals(item.getItemMeta().getDisplayName());
    }

    // Passa la TNT al giocatore target senza resettare il timer del round;
    // SVUOTA l'inventario del portatore precedente
    public void passTNT(Player newHolder) {
        if (!gameActive) return;
        if (currentHolder != null && currentHolder.equals(newHolder)) {
            return;
        }
        if (!activePlayers.contains(newHolder)) return;
        broadcast("La TNT è passata da " + currentHolder.getName() + " a " + newHolder.getName());
        currentHolder.getInventory().clear();
        removeTNT(currentHolder);
        currentHolder = newHolder;
        assignTNT(currentHolder);
    }

    // Avvia il timer globale del round
    private void startRoundTimer() {
        roundTask = Bukkit.getScheduler().runTaskTimer(TntTag.getInstance(), () -> {
            if (roundTimeRemaining <= 0) {
                eliminateCurrentHolder();
            } else {
                if (currentHolder != null && currentHolder.isOnline()) {
                    currentHolder.sendMessage("Tempo rimanente per passare la TNT: " + roundTimeRemaining + " secondi");
                }
                roundTimeRemaining--;
            }
        }, 20, 20);
    }

    // Elimina il giocatore portatore al termine del timer del round
    private void eliminateCurrentHolder() {
        if (currentHolder != null && currentHolder.isOnline()) {
            explosionInProgress = true;
            Location loc = currentHolder.getLocation();
            currentHolder.getWorld().createExplosion(loc, 4.0F, false, false);
            currentHolder.sendMessage("BOOM! La TNT è esplosa su di te!");
            broadcast("Il giocatore " + currentHolder.getName() + " è stato eliminato!");
            removeTNT(currentHolder);
            currentHolder.getInventory().clear();
            activePlayers.remove(currentHolder);
            teleportToSpawn(currentHolder);
            Bukkit.getScheduler().runTaskLater(TntTag.getInstance(), () -> explosionInProgress = false, 1L);
        }
        cancelRoundTimer();
        if (activePlayers.size() > 1) {
            broadcast("Nuovo round in partenza con " + activePlayers.size() + " giocatori!");
            roundTimeRemaining = roundDuration;
            chooseRandomHolder();
            startRoundTimer();
        } else if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            broadcast("Il vincitore è " + winner.getName() + "!");
            winner.getInventory().clear();
            teleportToSpawn(winner);
            winner.sendTitle(ChatColor.YELLOW + "HAI VINTO!", "", 10, 40, 10);
            stopGame();
        } else {
            broadcast("Nessun giocatore rimasto, partita terminata!");
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

    // Ferma il gioco: svuota l'inventario di tutti e teletrasporta allo spawn
    public void stopGame() {
        if (!gameActive) return;
        cancelRoundTimer();
        broadcast("La partita di TNTTag è terminata!");
        for (Player p : activePlayers) {
            p.getInventory().clear();
            teleportToSpawn(p);
        }
        gameActive = false;
        activePlayers.clear();
        currentHolder = null;
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

    // Gestisce la rimozione di un giocatore (per uscita o cambio mondo)
    public void removePlayer(Player p) {
        if (activePlayers.contains(p)) {
            activePlayers.remove(p);
            if (p.equals(currentHolder)) {
                removeTNT(p);
                broadcast("Il giocatore " + p.getName() + " è uscito ed era il portatore della TNT.");
                cancelRoundTimer();
                if (!activePlayers.isEmpty()) {
                    if (activePlayers.size() == 1) {
                        Player winner = activePlayers.get(0);
                        broadcast("Il vincitore è " + winner.getName() + "!");
                        winner.getInventory().clear();
                        teleportToSpawn(winner);
                        winner.sendTitle(ChatColor.YELLOW + "HAI VINTO!", "", 10, 40, 10);
                        stopGame();
                        return;
                    } else {
                        broadcast("Nuovo round in partenza con " + activePlayers.size() + " giocatori!");
                        roundTimeRemaining = roundDuration;
                        chooseRandomHolder();
                        startRoundTimer();
                    }
                } else {
                    broadcast("Nessun giocatore rimasto, partita terminata!");
                    stopGame();
                }
            }
            if (activePlayers.size() == 1 && gameActive) {
                Player winner = activePlayers.get(0);
                broadcast("Il vincitore è " + winner.getName() + "!");
                winner.getInventory().clear();
                teleportToSpawn(winner);
                winner.sendTitle(ChatColor.YELLOW + "HAI VINTO!", "", 10, 40, 10);
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
