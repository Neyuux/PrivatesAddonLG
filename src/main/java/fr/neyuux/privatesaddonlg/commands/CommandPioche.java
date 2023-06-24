package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.*;

public class CommandPioche implements CommandExecutor, Listener {

    private final HashMap<UUID, List<Location>> map = new HashMap<>();

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
                List<Location> blocks = new ArrayList<>();

                for (BlockFace face : new BlockFace[] {BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.NORTH_EAST, BlockFace.NORTH_NORTH_EAST, BlockFace.EAST_NORTH_EAST, BlockFace.UP, BlockFace.DOWN}) {
                    Block relative = block.getRelative(face);
                    relative.setType(Material.DIAMOND_ORE);
                    blocks.add(relative.getLocation());
                }

                player.sendMessage(Plugin.getPrefix() + "§bDiamant le plus proche : " + Utils.updateArrow(player, block.getLocation()));
                map.put(player.getUniqueId(), blocks);
            } else {
                player.sendMessage(Plugin.getPrefix() + "§bDiamant le plus proche : " + Utils.updateArrow(player, map.get(player.getUniqueId()).get(0)));
            }
        }

        return true;
    }


    @EventHandler
    public void onDiamondDamage(BlockDamageEvent ev) {
        Player player = ev.getPlayer();
        UUID id = player.getUniqueId();
        Block block = ev.getBlock();
        if (map.containsKey(id) && map.get(id).contains(block.getLocation()) && block.getType().equals(Material.DIAMOND_ORE)) {
            ev.setInstaBreak(false);
            ev.setCancelled(true);
            block.setType(Material.BEDROCK);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        while (it.hasNext()) {
            Block block = it.next();

            for (Map.Entry<UUID, List<Location>> entry : map.entrySet()) {
                if (entry.getValue().contains(block.getLocation()))
                    it.remove();
            }
        }
    }

    @EventHandler
    public void onStart(StartEvent ev) {
        map.clear();
    }
}
