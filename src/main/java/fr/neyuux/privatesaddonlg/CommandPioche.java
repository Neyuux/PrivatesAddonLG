package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPioche implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            WereWolfAPI game = Plugin.getINSTANCE().getGame();

            int rx = game.getRandom().nextInt(20) - 9;
            int rz = game.getRandom().nextInt(20) - 9;
            Block block = player.getLocation().clone().add(rx, 0, rz).getBlock();

            block.setType(Material.BEDROCK);
            block.getLocation().add(0, 1, 0).getBlock().setType(Material.SIGN_POST);

            org.bukkit.block.Sign sign =(Sign) block.getLocation().add(0, 1, 0).getBlock().getState();

            block.getState().update();
            sign.update(true, true);

            sign.setLine(0, "Gros bouffon");
            sign.setLine(1, "Fuck");
            sign.setLine(2, "Khqbib");

            block.getState().update();
            sign.update(true, true);

            player.sendMessage(Plugin.getPrefix() + "§bDiamant le plus proche : " + Utils.updateArrow(player, block.getLocation()));
        }

        return true;
    }
}
