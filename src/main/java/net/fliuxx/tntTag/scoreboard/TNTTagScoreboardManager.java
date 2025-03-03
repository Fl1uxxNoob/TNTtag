package net.fliuxx.tntTag.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

public class TNTTagScoreboardManager {
    private Scoreboard board;
    private Objective objective;
    private Team timerTeam;
    private Team aliveTeam;

    public TNTTagScoreboardManager() {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        board = sm.getNewScoreboard();
        // Il titolo della scoreboard: "TNT TAG" in rosso chiaro
        objective = board.registerNewObjective("TNTTagObj", "dummy", ChatColor.RED + "TNT TAG");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Creiamo due team fissi per il timer e per il conteggio delle vite
        timerTeam = board.registerNewTeam("timerTeam");
        aliveTeam = board.registerNewTeam("aliveTeam");

        // Utilizziamo entry univoche per ciascun team
        String timerEntry = ChatColor.GOLD.toString() + ChatColor.BOLD + "Timer";
        String aliveEntry = ChatColor.GREEN.toString() + ChatColor.BOLD + "Giocatori";

        timerTeam.addEntry(timerEntry);
        aliveTeam.addEntry(aliveEntry);

        // Impostiamo le posizioni nella sidebar (ordine)
        objective.getScore(timerEntry).setScore(2);
        objective.getScore(aliveEntry).setScore(1);
    }

    /**
     * Aggiorna la scoreboard impostando il timer del round e il numero di giocatori in vita.
     * @param timer il tempo rimanente del round (in secondi)
     * @param aliveCount il numero di giocatori attivi
     */
    public void updateScoreboard(int timer, int aliveCount) {
        timerTeam.setSuffix(ChatColor.WHITE + ": " + timer + "s");
        aliveTeam.setSuffix(ChatColor.WHITE + ": " + aliveCount);
    }

    public Scoreboard getScoreboard() {
        return board;
    }
}
