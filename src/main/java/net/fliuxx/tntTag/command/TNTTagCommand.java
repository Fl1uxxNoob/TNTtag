package net.fliuxx.tntTag.command;

import net.fliuxx.tntTag.gui.ArenaSelectionGUI;
import net.fliuxx.tntTag.manager.MessageManager;
import net.fliuxx.tntTag.manager.TNTTagManager;
import net.fliuxx.tntTag.gui.TNTTagGUI;
import net.fliuxx.tntTag.worldguard.ArenaWorldGuardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.fliuxx.tntTag.TntTag;

public class TNTTagCommand implements CommandExecutor {
    private TNTTagManager manager;
    private MessageManager messageManager;

    public TNTTagCommand(TNTTagManager manager) {
        this.manager = manager;
        this.messageManager = MessageManager.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(messageManager.getUsageMessage());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(messageManager.getNoPermissionMessage());
                    return true;
                }
                if (manager.isGameActive()) {
                    player.sendMessage(messageManager.getGameAlreadyActiveMessage());
                    return true;
                }
                manager.startGame();
                break;

            case "stop":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(messageManager.getNoPermissionMessage());
                    return true;
                }
                if (!manager.isGameActive()) {
                    player.sendMessage(messageManager.getNoGameActiveMessage());
                    return true;
                }
                manager.stopGame();
                player.sendMessage(messageManager.getGameStoppedMessage());
                break;

            case "reload":
                if (!player.hasPermission("tnttag.admin")) {
                    player.sendMessage(messageManager.getNoPermissionMessage());
                    return true;
                }
                TntTag.getInstance().reloadConfig();
                // Ricarica anche i messaggi
                MessageManager.getInstance().reloadMessages();
                TNTTagGUI.resetInstance();
                ArenaSelectionGUI.resetInstance();
                // Richiama il controllo delle impostazioni delle arene
                ArenaWorldGuardManager.checkAndApplySettings();
                player.sendMessage(messageManager.getConfigReloadedMessage());
                break;

            case "gui":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(messageManager.getNoPermissionMessage());
                    return true;
                }
                if (manager.isGameActive()) {
                    player.sendMessage(messageManager.getGameActiveGUIBlockedMessage());
                    return true;
                }
                TNTTagGUI gui = TNTTagGUI.getInstance(manager);
                gui.openGUI(player);
                break;

            default:
                player.sendMessage(messageManager.getUnknownCommandMessage());
                break;
        }
        return true;
    }
}