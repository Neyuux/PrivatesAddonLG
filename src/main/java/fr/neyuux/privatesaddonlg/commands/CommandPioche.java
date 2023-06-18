package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CommandPioche implements CommandExecutor, Listener {

    private final HashMap<UUID, Location> map = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            WereWolfAPI game = Plugin.getINSTANCE().getGame();

            if (game.getTimer() > 3000)
                return true;

            if (player.getLocation().getY() > 18.0D) {
                player.sendMessage(Plugin.getPrefix() + "§cVous devez être à la couche du diamant pour utiliser cette commande !");
                return true;
            }

            if (!map.containsKey(player.getUniqueId())) {

                int rx = game.getRandom().nextInt(40) - 19;
                int rz = game.getRandom().nextInt(40) - 19;
                Block block = player.getLocation().clone().add(rx, 0, rz).getBlock();

                block.setType(Material.DIAMOND_ORE);
                block.getLocation().add(0, 1, 0).getBlock().setType(Material.SIGN_POST);

                org.bukkit.block.Sign sign = (Sign) block.getLocation().add(0, 1, 0).getBlock().getState();

                block.getState().update();
                sign.update(true, true);

                sign.setLine(0, "Gros bouffon");
                sign.setLine(3, "(Fuck Khqbib)");

                block.getState().update();
                sign.update(true, true);

                player.sendMessage(Plugin.getPrefix() + "§bDiamant le plus proche : " + Utils.updateArrow(player, block.getLocation()));
                map.put(player.getUniqueId(), block.getLocation());
            } else {
                player.sendMessage(Plugin.getPrefix() + "§bDiamant le plus proche : " + Utils.updateArrow(player, map.get(player.getUniqueId())));
            }
        }

        return true;
    }


    @EventHandler
    public void onDiamondDamage(BlockDamageEvent ev) {
        Player player = ev.getPlayer();
        UUID id = player.getUniqueId();
        Block block = ev.getBlock();
        if (map.containsKey(id) && map.get(id).equals(block.getLocation()) && block.getType().equals(Material.DIAMOND_ORE)) {
            ev.setInstaBreak(false);
            ev.setCancelled(true);
            block.setType(Material.BEDROCK);
        }
    }

    @EventHandler
    public void onStart(StartEvent ev) {
        map.clear();
    }
}
