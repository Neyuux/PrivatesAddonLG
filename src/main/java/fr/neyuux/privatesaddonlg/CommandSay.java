package fr.neyuux.privatesaddonlg;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSay implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        Bukkit.broadcastMessage("");
        if (sender instanceof Player)
            Bukkit.broadcastMessage(" " + ((Player)sender).getDisplayName() + " §8» §b");
        Bukkit.broadcastMessage("");

        return true;
    }
}
