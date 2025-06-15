package net.fliuxx.tntTag.manager;

import net.fliuxx.tntTag.TntTag;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

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

    public List<String> getMessageList(String path) {
        List<String> messages = messagesConfig.getStringList(path);
        List<String> coloredMessages = new ArrayList<String>();
        for (String message : messages) {
            coloredMessages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        return coloredMessages;
    }

    public List<String> getMessageList(String path, String placeholder, String value) {
        List<String> messages = getMessageList(path);
        List<String> processedMessages = new ArrayList<String>();
        for (String message : messages) {
            processedMessages.add(message.replace("{" + placeholder + "}", value));
        }
        return processedMessages;
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

    // Metodi specifici per la GUI principale
    public String getMainGUITitle() {
        return getMessage("gui.main.title");
    }

    public String getTimeButtonName() {
        return getMessage("gui.main.time_button.name");
    }

    public List<String> getTimeButtonLore(int time) {
        return getMessageList("gui.main.time_button.lore", "time", String.valueOf(time));
    }

    public String getStartButtonName() {
        return getMessage("gui.main.start_button.name");
    }

    public List<String> getStartButtonLore() {
        return getMessageList("gui.main.start_button.lore");
    }

    public String getArenaButtonName() {
        return getMessage("gui.main.arena_button.name");
    }

    public List<String> getArenaButtonLore() {
        return getMessageList("gui.main.arena_button.lore");
    }

    // Metodi specifici per la GUI di selezione arena
    public String getArenaSelectionGUITitle() {
        return getMessage("gui.arena_selection.title");
    }

    public String getRandomOptionName() {
        return getMessage("gui.arena_selection.random_option.name");
    }

    public List<String> getRandomOptionLore() {
        return getMessageList("gui.arena_selection.random_option.lore");
    }

    public String getArenaItemName(String arenaName) {
        return getMessage("gui.arena_selection.arena_item.name", "arena_name", arenaName);
    }

    public List<String> getArenaItemLore() {
        return getMessageList("gui.arena_selection.arena_item.lore");
    }

    public String getPreviousPageName() {
        return getMessage("gui.arena_selection.navigation.previous_page.name");
    }

    public List<String> getPreviousPageLore() {
        return getMessageList("gui.arena_selection.navigation.previous_page.lore");
    }

    public String getNextPageName() {
        return getMessage("gui.arena_selection.navigation.next_page.name");
    }

    public List<String> getNextPageLore() {
        return getMessageList("gui.arena_selection.navigation.next_page.lore");
    }

    // Metodi per i messaggi della GUI
    public String getTimeIncreasedMessage(int time) {
        return getMessage("messages.gui.time_increased", "time", String.valueOf(time));
    }

    public String getTimeDecreasedMessage(int time) {
        return getMessage("messages.gui.time_decreased", "time", String.valueOf(time));
    }

    public String getArenaSelectedMessage(String arenaName) {
        return getMessage("messages.gui.arena_selected", "arena_name", arenaName);
    }

    public String getRandomSelectedMessage() {
        return getMessage("messages.gui.random_selected");
    }

    public String getCannotUseNowMessage() {
        return getMessage("messages.gui.cannot_use_now");
    }

    public String getGameActiveGUIBlockedMessage() {
        return getMessage("messages.gui.game_active_gui_blocked");
    }

    // Metodi per i messaggi dei comandi
    public String getPlayerOnlyMessage() {
        return getMessage("messages.commands.player_only");
    }

    public String getUsageMessage() {
        return getMessage("messages.commands.usage");
    }

    public String getNoPermissionMessage() {
        return getMessage("messages.commands.no_permission");
    }

    public String getGameAlreadyActiveMessage() {
        return getMessage("messages.commands.game_already_active");
    }

    public String getNoGameActiveMessage() {
        return getMessage("messages.commands.no_game_active");
    }

    public String getGameStoppedMessage() {
        return getMessage("messages.commands.game_stopped");
    }

    public String getConfigReloadedMessage() {
        return getMessage("messages.commands.config_reloaded");
    }

    public String getUnknownCommandMessage() {
        return getMessage("messages.commands.unknown_command");
    }

    // Metodi per i messaggi di WorldGuard
    public String getWorldGuardNotFoundMessage() {
        return getMessage("messages.worldguard.worldguard_not_found");
    }

    public String getWorldGuardAccessErrorMessage() {
        return getMessage("messages.worldguard.worldguard_access_error");
    }

    public String getNoArenaDefinedMessage() {
        return getMessage("messages.worldguard.no_arenas_defined");
    }

    public String getArenaFoundMessage(String arenaName, String worldName) {
        return getMessage("messages.worldguard.arena_found", new String[]{"arena_name", "world_name"}, new String[]{arenaName, worldName});
    }

    public String getWorldNotFoundMessage(String worldName, String arenaName) {
        return getMessage("messages.worldguard.world_not_found", new String[]{"world_name", "arena_name"}, new String[]{worldName, arenaName});
    }

    public String getApplyingSettingsMessage(String worldName) {
        return getMessage("messages.worldguard.applying_settings", "world_name", worldName);
    }

    public String getRegionManagerErrorMessage(String worldName) {
        return getMessage("messages.worldguard.region_manager_error", "world_name", worldName);
    }

    public String getGlobalRegionNotFoundMessage(String worldName) {
        return getMessage("messages.worldguard.global_region_not_found", "world_name", worldName);
    }

    public String getApplyingFlagsMessage(String worldName) {
        return getMessage("messages.worldguard.applying_flags", "world_name", worldName);
    }

    public String getFlagsSavedMessage(String worldName) {
        return getMessage("messages.worldguard.flags_saved", "world_name", worldName);
    }

    public String getFlagsSaveErrorMessage(String worldName, String error) {
        return getMessage("messages.worldguard.flags_save_error", new String[]{"world_name", "error"}, new String[]{worldName, error});
    }

    public String getFlagsErrorMessage(String worldName, String error) {
        return getMessage("messages.worldguard.flags_error", new String[]{"world_name", "error"}, new String[]{worldName, error});
    }

    public String getApplyingGamerulesMessage(String worldName) {
        return getMessage("messages.worldguard.applying_gamerules", "world_name", worldName);
    }

    public String getGamerulesAppliedMessage(String worldName) {
        return getMessage("messages.worldguard.gamerules_applied", "world_name", worldName);
    }

    public String getGamerulesErrorMessage(String worldName, String error) {
        return getMessage("messages.worldguard.gamerules_error", new String[]{"world_name", "error"}, new String[]{worldName, error});
    }

    public String getConfigurationCompletedMessage(int count) {
        return getMessage("messages.worldguard.configuration_completed", "count", String.valueOf(count));
    }
}