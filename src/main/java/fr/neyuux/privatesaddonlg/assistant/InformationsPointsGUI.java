package fr.neyuux.privatesaddonlg.assistant;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import fr.ph1lou.werewolfapi.utils.Wrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
                .filter(roleRegister -> main.getAssistant().getCompo().getInformationsPoints().keySet().stream().anyMatch(s -> Arrays.asList(roleRegister.getMetaDatas().key().split("\\.")).contains(s)))
                .sorted((o1, o2) -> Plugin.getRoleTranslated(o1.getMetaDatas().key()).compareToIgnoreCase(Plugin.getRoleTranslated(o2.getMetaDatas().key())))
                .forEach(roleRegister -> {
                    WereWolfAPI game = Plugin.getINSTANCE().getGame();
                    boolean isActivated = game.getConfig().getRoleCount(roleRegister.getMetaDatas().key()) > 0;
                    @SuppressWarnings("OptionalGetWithoutIsPresent") int points = main.getAssistant().getCompo().getInformationsPoints().get(main.getAssistant().getCompo().getInformationsPoints().keySet().stream().filter(s -> Arrays.asList(roleRegister.getMetaDatas().key().split("\\.")).contains(s)).findFirst().get());
                    int number = game.getConfig().getRoleCount(roleRegister.getMetaDatas().key());

                    Optional<String> incompatible = Arrays.stream(roleRegister.getMetaDatas().incompatibleRoles()).filter(s -> game.getConfig().getRoleCount(s) > 0).map(game::translate).findFirst();

                    i.addAndGet(number);
                    infopoints.addAndGet(roleRegister.getMetaDatas().requireDouble() && number != 0 ? points : number * points);

                    items.add(ClickableItem.of(this.getRoleItem(roleRegister, isActivated, points, number, incompatible.orElse(null)), ev -> {

                        String key = roleRegister.getMetaDatas().key();
                        AtomicBoolean unRemovable = new AtomicBoolean(false);

                        main.getRegisterManager().getRolesRegister()
                                .stream()
                                .filter(roleRegister1 -> Arrays.stream(roleRegister1.getMetaDatas().requireRoles())
                                        .anyMatch(requiredRole -> requiredRole.equals(roleRegister1.getMetaDatas().key())))
                                .map(iRoleRoleWrapper -> iRoleRoleWrapper.getMetaDatas().key())
                                .filter(roleRegister1Key -> game.getConfig().getRoleCount(roleRegister1Key) > 0)
                                .findFirst()
                                .ifPresent(role -> unRemovable.set(true));

                        if (game.getConfig().getRoleCount(key) > 0) {
                            if (ev.isLeftClick()) {
                                this.selectPlus(game, key);
                            } else if (ev.isRightClick()) {
                                int count = game.getConfig().getRoleCount(key);
                                if (!unRemovable.get() || count > 1) {
                                    if (roleRegister.getMetaDatas().requireDouble() && count == 2) {
                                        this.selectMinus(game, key);
                                    }
                                    this.selectMinus(game, key);
                                }
                            }
                        } else {
                            if (ev.isLeftClick()) {
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


    private ItemStack getRoleItem(Wrapper<IRole, Role> datas, boolean isActivated, int point, int number, String incompatible) {
        ChatColor color = (isActivated ? ChatColor.GREEN : ChatColor.RED);

        List<String> lore = new ArrayList<>();

        List<String> unremovable = main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister1 -> Arrays.stream(roleRegister1.getMetaDatas().requireRoles())
                        .anyMatch(requiredRole -> requiredRole.equals(roleRegister1.getMetaDatas().key())))
                .map(iRoleRoleWrapper -> iRoleRoleWrapper.getMetaDatas().key())
                .filter(roleRegister1Key -> main.getGame().getConfig().getRoleCount(roleRegister1Key) > 0)
                .collect(Collectors.toList());

        lore.add("§fNombre de Points : §b§l" + point);
        lore.add("");
        lore.add("§fCatégorie : " + main.getGame().translate(datas.getMetaDatas().category().getChatColor()) + main.getGame().translate(datas.getMetaDatas().category().getKey()));
        lore.add("§fAttribut : §b§l" + main.getGame().translate(datas.getMetaDatas().attribute().getKey()));
        lore.add("§fAura : " + main.getGame().translate(datas.getMetaDatas().defaultAura().getKey()));
        lore.add("§fProvenance : §b§l" + main.getGame().translate(datas.getAddonKey()));
        lore.add("");

        if (datas.getMetaDatas().requireRoles() != null && datas.getMetaDatas().requireRoles().length > 0) {
            lore.add("§fRôles nécessaires : ");
            Arrays.stream(datas.getMetaDatas().requireRoles()).forEach(roleKey -> lore.add(" §3§l■ §b" + main.getGame().translate(roleKey)));
            lore.add("");
        }

        if (!unremovable.isEmpty()) {
            unremovable.forEach(roleKey -> lore.add("§fNon-supprimable car §b" + main.getGame().translate(roleKey) + " §fchargé"));
            lore.add("");
        }

        if (incompatible != null && !incompatible.equals("")) {
            lore.add("§fIncompatible avec le rôle : §b§l" + main.getGame().translate(incompatible));
            lore.add("");
        }

        lore.add("§7>>Clic droit pour §cretirer");
        lore.add("§7>>Clic gauche pour §aajouter");


        return new ItemBuilder(isActivated ? UniversalMaterial.GREEN_TERRACOTTA.getStack() : UniversalMaterial.RED_TERRACOTTA.getStack())
                .setDisplayName(color + Plugin.getRoleTranslated(datas.getMetaDatas().key()))
                .setLore(lore)
                .setAmount(number)
                .build();
    }

    public void selectMinus(WereWolfAPI game, String key) {
        if (game.isState(StateGame.GAME)) {
            return;
        }
        IConfiguration config = game.getConfig();
        if (config.getRoleCount(key) > 0) {
            game.setTotalRoles(game.getTotalRoles() - 1);
            config.removeOneRole(key);
        }
    }

    public void selectPlus(WereWolfAPI game, String key) {
        if (game.isState(StateGame.GAME)) {
            return;
        }
        IConfiguration config = game.getConfig();
        config.addOneRole(key);
        game.setTotalRoles(game.getTotalRoles() + 1);
    }
}
