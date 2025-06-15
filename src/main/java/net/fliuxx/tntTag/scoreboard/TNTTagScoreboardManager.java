package net.fliuxx.tntTag.scoreboard;

import net.fliuxx.tntTag.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

public class TNTTagScoreboardManager {
    private Scoreboard board;
    private Objective objective;
    private Team timerTeam;
    private Team aliveTeam;
    private MessageManager messageManager;

    public TNTTagScoreboardManager() {
        messageManager = MessageManager.getInstance();

        ScoreboardManager sm = Bukkit.getScoreboardManager();
        board = sm.getNewScoreboard();

        // Il titolo della scoreboard utilizzando il MessageManager
        objective = board.registerNewObjective("TNTTagObj", "dummy");
        objective.setDisplayName(messageManager.getScoreboardTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Creiamo due team fissi per il timer e per il conteggio delle vite
        timerTeam = board.registerNewTeam("timerTeam");
        aliveTeam = board.registerNewTeam("aliveTeam");

        // Utilizziamo entry univoche per ciascun team usando i messaggi configurabili
        String timerEntry = messageManager.getScoreboardTimerLabel();
        String aliveEntry = messageManager.getScoreboardPlayersLabel();

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
        timerTeam.setSuffix(messageManager.getScoreboardTimerSuffix(timer));
        aliveTeam.setSuffix(messageManager.getScoreboardPlayersSuffix(aliveCount));
    }

    public Scoreboard getScoreboard() {
        return board;
    }
}