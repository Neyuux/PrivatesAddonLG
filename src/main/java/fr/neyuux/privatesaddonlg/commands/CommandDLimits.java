package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.EnhancedDiamondLimit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDLimits implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player)
            EnhancedDiamondLimit.ChangeLevelGUI.INVENTORY.open((Player) sender);
        return true;
    }

}
