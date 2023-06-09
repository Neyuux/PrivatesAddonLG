package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import fr.ph1lou.werewolfapi.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

            Bukkit.getScheduler().runTaskLater(Plugin.getINSTANCE(), () -> {

                if (block.getType() != Material.DIAMOND_ORE)
                    return;

                block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, block.getTypeId(), 20);

                block.setType(Material.AIR);

                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5 , 0.5), new ItemBuilder(Material.DIAMOND)
                        .setLore(":)").build());
            }, 4L);
        }
    }

    @EventHandler
    public void onFakeDiamondSpawn(ItemSpawnEvent ev) {
        Item entityItem = ev.getEntity();

        if (this.map.values().stream().anyMatch(locations -> locations.stream().anyMatch(location -> location.distanceSquared(entityItem.getLocation()) <= 1))) {
            ItemStack item = ev.getEntity().getItemStack();

            if (item != null && item.getType() == Material.DIAMOND) {
                if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
                    entityItem.remove();
            }
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
    public void onClickFakeDiamond(InventoryClickEvent ev) {
        ItemStack item = ev.getCurrentItem();

        if (item == null || item.getType() != Material.DIAMOND)
            return;

        if (item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().stream().anyMatch(s -> s.contains(":)"))) {
            item.setType(Material.BONE);
            item.setAmount(64);

            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§fBon toutou ♥");
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onStart(StartEvent ev) {
        map.clear();
    }
}
