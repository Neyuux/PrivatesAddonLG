package fr.neyuux.privatesaddonlg.assistant;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class InformationsPointsGUI implements InventoryProvider {

    private final Plugin main;

    public InformationsPointsGUI(Plugin main) {
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(3, 5, ClickableItem.of(new ItemBuilder(Material.COMPASS)
                .setDisplayName("§fFermer l'inventaire")
                .build(), ev -> ev.getWhoClicked().closeInventory()));

        final Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(9 * 3);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        AtomicInteger i = new AtomicInteger();
        AtomicInteger infopoints = new AtomicInteger();
        final Pagination pagination = contents.pagination();
        final SlotIterator roleIterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
        final List<ClickableItem> items = new ArrayList<>();

        main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> main.getAssistant().getCompo().getInformationsPoints().keySet().stream().anyMatch(s -> roleRegister.getMetaDatas().key().contains(s)))
                .sorted((o1, o2) -> Plugin.getRoleTranslated(o1.getMetaDatas().key()).compareToIgnoreCase(Plugin.getRoleTranslated(o2.getMetaDatas().key())))
                .forEach(roleRegister -> {
                    WereWolfAPI game = Plugin.getINSTANCE().getGame();
                    boolean isActivated = game.getConfig().getRoleCount(roleRegister.getMetaDatas().key()) > 0;
                    ChatColor color = (isActivated ? ChatColor.GREEN : ChatColor.RED);
                    int points = main.getAssistant().getCompo().getInformationsPoints().get(main.getAssistant().getCompo().getInformationsPoints().keySet().stream().filter(s -> roleRegister.getMetaDatas().key().contains(s)).findFirst().get());
                    int number = game.getConfig().getRoleCount(roleRegister.getMetaDatas().key());

                    i.addAndGet(number);
                    infopoints.addAndGet(number * points);

                    items.add(ClickableItem.of(new ItemBuilder((isActivated ? UniversalMaterial.GREEN_TERRACOTTA.getStack() : UniversalMaterial.RED_TERRACOTTA.getStack()))
                            .setDisplayName(color + game.translate(roleRegister.getMetaDatas().key()))
                            .setLore(Arrays.asList("§fNombre de Points : §b§l" + points, "", ">>§7Clic droit pour §cretirer", "§7>>Clic gauche pour §aajouter"))
                            .setAmount(game.getConfig().getRoleCount(roleRegister.getMetaDatas().key()))
                            .build(), ev -> {
                        String key = roleRegister.getMetaDatas().key();
                        AtomicBoolean unRemovable = new AtomicBoolean(false);
                        ArrayList<String> lore = new ArrayList<>(Arrays.asList(game.translate("werewolf.menus.lore.left"), game.translate("werewolf.menus.lore.right")));
                        ArrayList<String> lore2 = new ArrayList<>(lore);
                        lore2.add(game.translate("werewolf.menus.lore.shift"));
                        Aura aura = roleRegister.getMetaDatas().defaultAura();
                        lore2.add(game.translate("werewolf.commands.player.aura.menu_role", Formatter.format("&aura&", game.translate(aura.getKey()))));
                        RoleAttribute roleAttribute = roleRegister.getMetaDatas().attribute();
                        lore2.add(game.translate("werewolf.attributes.menu", Formatter.format("&attribute&", game.translate(roleAttribute.getKey()))));
                        Arrays.stream(roleRegister.getMetaDatas().requireRoles()).forEach(roleKey -> lore2.add(game.translate("werewolf.menus.roles.need", Formatter.role(game.translate(roleKey)))));
                        main.getRegisterManager().getRolesRegister().stream().filter(roleRegister1 -> Arrays.stream(roleRegister1.getMetaDatas().requireRoles()).anyMatch(requiredRole -> requiredRole.equals(roleRegister1.getMetaDatas().key()))).map(iRoleRoleWrapper -> iRoleRoleWrapper.getMetaDatas().key()).filter(roleRegister1Key -> game.getConfig().getRoleCount(roleRegister1Key) > 0).findFirst().ifPresent(role -> {
                            lore2.add(game.translate("werewolf.menus.roles.dependant_load", Formatter.role(game.translate(role))));
                            unRemovable.set(true);
                        });
                        Optional<String> incompatible = Arrays.stream(roleRegister.getMetaDatas().incompatibleRoles()).filter(s -> game.getConfig().getRoleCount(s) > 0).map(game::translate).findFirst();
                        incompatible.ifPresent(role -> lore2.add(game.translate("werewolf.menus.roles.incompatible", Formatter.role(role))));
                        if (game.getConfig().getRoleCount(key) > 0) {
                            items.add(ClickableItem.of(new ItemBuilder(UniversalMaterial.GREEN_TERRACOTTA.getStack()).setAmount(game.getConfig().getRoleCount(key)).setLore(lore2).setDisplayName(game.translate(key)).build(), e -> {
                                if (e.isLeftClick()) {
                                    this.selectPlus(game, key);
                                } else if (e.isRightClick()) {
                                    int count = game.getConfig().getRoleCount(key);
                                    if (!unRemovable.get() || count > 1) {
                                        if (roleRegister.getMetaDatas().requireDouble() && count == 2) {
                                            this.selectMinus(game, key);
                                        }
                                        this.selectMinus(game, key);
                                    }
                                }
                            }));
                        } else {
                            items.add(ClickableItem.of(new ItemBuilder(UniversalMaterial.RED_TERRACOTTA.getStack()).setAmount(1).setLore(lore2).setDisplayName(game.translate(key)).build(), e -> {
                                if (e.isLeftClick()) {
                                    if (incompatible.isPresent()) {
                                        return;
                                    }
                                    if (Arrays.stream(roleRegister.getMetaDatas().requireRoles()).anyMatch(requireRole -> game.getConfig().getRoleCount(requireRole) == 0)) {
                                        return;
                                    }
                                    if (roleRegister.getMetaDatas().requireDouble()) {
                                        this.selectPlus(game, key);
                                    }
                                    this.selectPlus(game, key);
                                }
                            }));
                        }
                    }));
                });

        pagination.setItems(items.toArray(new ClickableItem[0]));
        pagination.addToIterator(roleIterator);

        if (!pagination.isFirst())
            contents.set(3, 1, ClickableItem.of(new ItemBuilder(Material.PAPER).setDisplayName("Page Précédante").build(), onClick -> INVENTORY.open((Player) onClick.getWhoClicked(), pagination.previous().getPage()))); //previous page

        if (!pagination.isLast())
            contents.set(3, 7, ClickableItem.of(new ItemBuilder(Material.PAPER).setDisplayName("Page Suivante").build(), onClick -> INVENTORY.open((Player) onClick.getWhoClicked(), pagination.next().getPage()))); //next page

        contents.set(3, 3, ClickableItem.empty(new ItemBuilder(Material.BEACON)
                .setDisplayName("Nombre de Rôles à Informations : §b§l" + i.get())
                .setAmount(i.get())
                .build()));

        contents.set(3, 4, ClickableItem.empty(new ItemBuilder(Material.SIGN)
                .setDisplayName("Nombre de Points : §b§l" + infopoints.get())
                .setAmount(infopoints.get())
                .build()));
    }

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("InformationsPointsGUI")
            .provider(new InformationsPointsGUI(Plugin.getINSTANCE()))
            .size(4, 9)
            .title("§bRôles à Informations")
            .closeable(true)
            .manager(Plugin.getINSTANCE().getInvManager())
            .build();

    public void selectMinus(WereWolfAPI game, String key) {
        if (game.isState(StateGame.GAME)) {
            return;
        }
        IConfiguration config = game.getConfig();
        if (config.getRoleCount(key) > 0) {
            //TODO game.setRoleInitialSize(game.getRoleInitialSize() - 1);
            config.removeOneRole(key);
        }
    }

    public void selectPlus(WereWolfAPI game, String key) {
        if (game.isState(StateGame.GAME)) {
            return;
        }
        IConfiguration config = game.getConfig();
        config.addOneRole(key);
        //TODO game.setRoleInitialSize(game.getRoleInitialSize() + 1);
    }
}
