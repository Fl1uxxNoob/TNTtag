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
import org.bukkit.ChatColor;

public class TNTTagCommand implements CommandExecutor {
    private TNTTagManager manager;

    public TNTTagCommand(TNTTagManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere eseguito solo da un giocatore.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /tnttag <start|stop|reload|gui>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(ChatColor.RED + "Non hai il permesso per eseguire questo comando!");
                    return true;
                }
                if (manager.isGameActive()) {
                    player.sendMessage(ChatColor.RED + "La partita è già in corso!");
                    return true;
                }
                manager.startGame();
                break;
            case "stop":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(ChatColor.RED + "Non hai il permesso per eseguire questo comando!");
                    return true;
                }
                if (!manager.isGameActive()) {
                    player.sendMessage(ChatColor.RED + "Nessuna partita in corso!");
                    return true;
                }
                manager.stopGame();
                player.sendMessage(ChatColor.GREEN + "Gioco TNTTag fermato!");
                break;
            case "reload":
                if (!player.hasPermission("tnttag.admin")) {
                    player.sendMessage(ChatColor.RED + "Non hai il permesso per eseguire questo comando!");
                    return true;
                }
                TntTag.getInstance().reloadConfig();
                // Ricarica anche i messaggi
                MessageManager.getInstance().reloadMessages();
                TNTTagGUI.resetInstance();
                ArenaSelectionGUI.resetInstance();
                // Richiama il controllo delle impostazioni delle arene
                ArenaWorldGuardManager.checkAndApplySettings();
                player.sendMessage(ChatColor.GREEN + "Configurazione, messaggi, GUI e impostazioni arene ricaricate!");
                break;
            case "gui":
                if (!player.hasPermission("tnttag.host")) {
                    player.sendMessage(ChatColor.RED + "Non hai il permesso per eseguire questo comando!");
                    return true;
                }
                if (manager.isGameActive()) {
                    player.sendMessage(ChatColor.RED + "Non puoi aprire la GUI mentre la TNTTag è attiva!");
                    return true;
                }
                TNTTagGUI gui = TNTTagGUI.getInstance(manager);
                gui.openGUI(player);
                break;
            default:
                player.sendMessage(ChatColor.YELLOW + "Comando non riconosciuto! Uso: /tnttag <start|stop|reload|gui>");
                break;
        }
        return true;
    }
}