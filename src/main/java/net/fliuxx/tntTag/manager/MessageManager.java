package net.fliuxx.tntTag.manager;

import net.fliuxx.tntTag.TntTag;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageManager {
    private static MessageManager instance;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    private MessageManager() {
        loadMessages();
    }

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }

    private void loadMessages() {
        messagesFile = new File(TntTag.getInstance().getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            TntTag.getInstance().saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Carica i valori di default dal file interno se non esistono
        InputStream defConfigStream = TntTag.getInstance().getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messagesConfig.setDefaults(defConfig);
        }
    }

    public void reloadMessages() {
        loadMessages();
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            return "Missing message: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String placeholder, String value) {
        String message = getMessage(path);
        return message.replace("{" + placeholder + "}", value);
    }

    public String getMessage(String path, String[] placeholders, String[] values) {
        String message = getMessage(path);
        if (placeholders.length != values.length) {
            return message;
        }

        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace("{" + placeholders[i] + "}", values[i]);
        }

        return message;
    }

    // Metodi specifici per la scoreboard
    public String getScoreboardTitle() {
        return getMessage("scoreboard.title");
    }

    public String getScoreboardTimerLabel() {
        return getMessage("scoreboard.timer_label");
    }

    public String getScoreboardPlayersLabel() {
        return getMessage("scoreboard.players_label");
    }

    public String getScoreboardTimerSuffix(int time) {
        return getMessage("scoreboard.timer_suffix", "time", String.valueOf(time));
    }

    public String getScoreboardPlayersSuffix(int count) {
        return getMessage("scoreboard.players_suffix", "count", String.valueOf(count));
    }
}