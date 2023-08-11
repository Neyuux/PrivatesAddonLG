package fr.neyuux.privatesaddonlg.commands.roles;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import fr.ph1lou.werewolfapi.versions.VersionUtils_1_8;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@RoleCommand(key="privatesaddon.roles.charmer.itemcommand", roleKeys={"werewolf.roles.charmer.display"}, argNumbers={0})
public class ItemCommand
        implements ICommandRole, Listener {

    private static final HashSet<UUID> alreadyUsed = new HashSet<>();

    private static final String NOT_USABLE = "§cCet objet n'est pas utilisable.";

    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {
        Player player = Bukkit.getPlayer(playerWW.getUUID());

        if (player == null)
            return;

        if (!alreadyUsed.contains(playerWW.getUUID())) {
            ItemCommandGUI.INVENTORY.open(player);
        } else {
            playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§cVous ne pouvez plus utiliser cette commande !"));
        }

        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null)
                if (this.isNotUsableItem(itemStack))
                    player.getInventory().remove(itemStack);
        }
    }

    @EventHandler
    public void onEnd(WinEvent ev) {
        alreadyUsed.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (!ev.hasItem())
            return;

        if (this.isNotUsableItem(ev.getItem()))
            ev.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent ev) {
        for (ItemStack itemStack : ev.getInventory()) {
            if (this.isNotUsableItem(itemStack))
                ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent ev) {
        if (!ev.getInventory().getType().equals(InventoryType.ANVIL))
            return;

        for (ItemStack itemStack : ev.getInventory()) {
            if (this.isNotUsableItem(itemStack))
                ev.setCancelled(true);
        }
    }


    private boolean isNotUsableItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(NOT_USABLE);
    }


    private static final class ItemCommandGUI implements InventoryProvider {

        public static final SmartInventory INVENTORY = SmartInventory.builder()
                .id("global")
                .manager(Plugin.getINSTANCE().getInvManager())
                .provider(new ItemCommandGUI())
                .size(3, 9)
                .title("§7Choisir un objet falsifié")
                .closeable(true)
                .build();

        private final WereWolfAPI game = Plugin.getINSTANCE().getGame();

        @Override
        public void init(Player player, InventoryContents inventoryContents) {
            game.getPlayersWW().stream()
                    .map(IPlayerWW::getRole)
                    .filter(role -> !game.getStuffs().getStuffRole(role.getKey()).isEmpty())
                    .forEach(role -> inventoryContents.add(ClickableItem.of(this.getRoleItem(role.getKey()),
                            ev -> {
                                for (ItemStack itemStack : game.getStuffs().getStuffRole(role.getKey())) {
                                    if (itemStack == null)
                                        continue;
                                    ItemMeta meta = itemStack.getItemMeta();
                                    meta.setLore(Collections.singletonList(NOT_USABLE));
                                    itemStack.setItemMeta(meta);

                                    ev.getWhoClicked().getInventory().addItem(itemStack);

                                    VersionUtils_1_8.getVersionUtils().sendTitle((Player) ev.getWhoClicked(),
                                            "§fStuff §b" + Plugin.getRoleTranslated(role.getKey()),
                                            "§cUtilisez /ww item pour supprimer ces items",
                                            5, 80, 5);
                                    ev.getWhoClicked().sendMessage(Plugin.getPrefix() + "§cUtilisez /ww item à nouveau pour supprimer ces objets de votre inventaire !");

                                    ev.getWhoClicked().closeInventory();

                                    alreadyUsed.add(ev.getWhoClicked().getUniqueId());
                                }
                            })));
        }

        @Override
        public void update(Player player, InventoryContents contents) {

        }


        private ItemStack getRoleItem(String key) {
            ItemBuilder item = new ItemBuilder(Material.BEACON);
            List<String> lore = new ArrayList<>(Arrays.asList("§7Permet d'obtenir les objets de ce rôle.",
                    "§7Vous ne pourrez cependant pas les utiliser.",
                    "",
                    "§7Contenu : "));

            for (ItemStack itemStack : game.getStuffs().getStuffRole(key)) {
                if (itemStack == null)
                    continue;
                lore.add("§7x§f" + itemStack.getAmount() + " " + itemStack.getType());
            }
            lore.add("");
            lore.add("§7>>Cliquez pour obtenir ces objets");

            item.setDisplayName("§b" + game.translate(key));
            item.setLore(lore);

            return item.build();
        }
    }
}