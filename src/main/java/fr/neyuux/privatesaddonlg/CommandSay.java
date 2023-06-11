package fr.neyuux.privatesaddonlg;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CommandSay implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        Bukkit.broadcastMessage("");
        if (sender instanceof Player) {
            Bukkit.broadcastMessage(" " + ((Player)sender).getDisplayName() + " §8» §b" + Arrays.stream(args).map(part -> part + " ").collect(Collectors.joining()));
        }
        Bukkit.broadcastMessage("");

        return true;
    }
}
