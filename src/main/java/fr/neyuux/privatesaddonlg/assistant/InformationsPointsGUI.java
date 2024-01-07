package fr.neyuux.privatesaddonlg.assistant;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class InformationsPointsGUI implements InventoryProvider {

    private final Plugin main;

    public InformationsPointsGUI(Plugin main) {
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(3, 8, ClickableItem.of(new ItemBuilder(Material.COMPASS)
                .setDisplayName("§fFermer l'inventaire")
                .build(), ev -> {
            ev.getWhoClicked().closeInventory();
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        AtomicInteger i = new AtomicInteger();
        AtomicInteger infopoints = new AtomicInteger();

        main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> main.getAssistant().getCompo().getInformationsPoints().keySet().stream().anyMatch(s -> roleRegister.getMetaDatas().key().contains(s)))
                .forEach(roleRegister -> {
                    boolean isActivated = main.getGame().getConfig().getRoleCount(roleRegister.getMetaDatas().key()) > 0;
                    ChatColor color = (isActivated ? ChatColor.GREEN : ChatColor.RED);
                    int points = main.getAssistant().getCompo().getInformationsPoints().get(main.getAssistant().getCompo().getInformationsPoints().keySet().stream().filter(s -> roleRegister.getMetaDatas().key().contains(s)).findFirst().get());
                    int number = main.getGame().getConfig().getRoleCount(roleRegister.getMetaDatas().key());

                    i.addAndGet(number);
                    infopoints.addAndGet(number * points);

                    contents.add(ClickableItem.of(new ItemBuilder((isActivated ? UniversalMaterial.GREEN_TERRACOTTA.getStack() : UniversalMaterial.RED_TERRACOTTA.getStack()))
                            .setDisplayName(color + main.getGame().translate(roleRegister.getMetaDatas().key()))
                            .setLore(Arrays.asList("§fNombre de Points : §b§l" + points, "", ">>§7Clic droit pour §cretirer", "§7>>Clic gauche pour §aajouter"))
                            .setAmount(main.getGame().getConfig().getRoleCount(roleRegister.getMetaDatas().key()))
                            .build(), ev -> {
                        if (ev.isLeftClick()) {
                            main.getGame().getConfig().addOneRole(roleRegister.getMetaDatas().key());
                        } else if (ev.isRightClick()) {
                            main.getGame().getConfig().removeOneRole(roleRegister.getMetaDatas().key());
                        }
                    }));
                });

        contents.set(3, 2, ClickableItem.empty(new ItemBuilder(Material.BEACON)
                .setDisplayName("Nombre de Rôles à Informations : §b§l" + i.get())
                .build()));

        contents.set(3, 5, ClickableItem.empty(new ItemBuilder(Material.SIGN)
                .setDisplayName("Nombre de Points : §b§l" + infopoints.get())
                .build()));
    }

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("InformationsPointsGUI")
            .provider(new InformationsPointsGUI(Plugin.getINSTANCE()))
            .size(4, 9)
            .title("§bRôles à Informations")
            .closeable(true)
            .build();
}
