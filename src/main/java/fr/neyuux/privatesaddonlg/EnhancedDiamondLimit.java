package fr.neyuux.privatesaddonlg;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.neyuux.privatesaddonlg.events.ArmorEquipEvent;
import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalJoinEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.enhanced_diamond_limit.name", defaultValue = true))
public class EnhancedDiamondLimit extends ListenerWerewolf {

    public final static HashMap<UUID, LimitLevel> levels = new HashMap<>();
    final Map<String, Integer> diamondPerPlayer = new HashMap<>();

    public EnhancedDiamondLimit(WereWolfAPI main) {
        super(main);
    }

    @Override
    public void register(boolean isActive) {
        super.register(isActive);

        File file = new File(Plugin.getINSTANCE().getDataFolder(), "diamondLimitLevel.yml");
        YamlConfiguration yconfig = YamlConfiguration.loadConfiguration(file);
        try {
            yconfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.getGame().getPlayersWW().forEach(iPlayerWW -> {
            if (yconfig.contains(iPlayerWW.getUUID().toString())) {
                levels.put(iPlayerWW.getUUID(), LimitLevel.getByLevel(yconfig.getInt(iPlayerWW.getUUID().toString())));
            } else {
                levels.put(iPlayerWW.getUUID(), LimitLevel.LEVEL_2);
                EnhancedDiamondLimit.updateConfig(iPlayerWW.getUUID());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        WereWolfAPI game = this.getGame();

        if (game.isState(StateGame.LOBBY))
            return;

        String playerName = event.getPlayer().getName();
        LimitLevel limitLevel = EnhancedDiamondLimit.levels.get(event.getPlayer().getUniqueId());
        Block block = event.getBlock();

        if (!block.getType().equals(Material.DIAMOND_ORE))
            return;

        Location loc = new Location(block.getWorld(), block.getLocation().getBlockX() + 0.5D, block.getLocation().getBlockY() + 0.5D, block.getLocation().getBlockZ() + 0.5D);

        if (!VersionUtils.getVersionUtils().getItemInHand(event.getPlayer()).getType().equals(Material.DIAMOND_PICKAXE) &&
                !VersionUtils.getVersionUtils().getItemInHand(event.getPlayer()).getType().equals(Material.IRON_PICKAXE))
            return;

        if (this.diamondPerPlayer.getOrDefault(playerName, 0) >= limitLevel.getDiamonds()) {
            block.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, this.getOreRandom(limitLevel.getGoldBoostPercentage())));
            block.getWorld().spawn(loc, ExperienceOrb.class).setExperience(event.getExpToDrop());
            block.setType(Material.AIR);

        } else if (this.diamondPerPlayer.getOrDefault(playerName, 0) + 1 < limitLevel.getDiamonds() && this.getGame().getRandom().nextDouble() <= limitLevel.getDiamondBoostPercentage()) {
            block.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND, 1));
            this.diamondPerPlayer.put(playerName, this.diamondPerPlayer.getOrDefault(playerName, 0) + 1);
        }

        this.diamondPerPlayer.put(playerName, this.diamondPerPlayer.getOrDefault(playerName, 0) + 1);
    }

    @EventHandler
    public void onFinalJoin(FinalJoinEvent ev) {
        File file = new File(Plugin.getINSTANCE().getDataFolder(), "diamondLimitLevel.yml");
        YamlConfiguration yconfig = YamlConfiguration.loadConfiguration(file);
        UUID uuid = ev.getPlayerWW().getUUID();

        if (yconfig.contains(uuid.toString())) {
            levels.put(uuid, LimitLevel.getByLevel(yconfig.getInt(uuid.toString())));
        } else {
            levels.put(uuid, LimitLevel.LEVEL_2);
            EnhancedDiamondLimit.updateConfig(uuid);
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent ev) {
        if (ev.getNewArmorPiece() != null && ev.getNewArmorPiece().getType().name().contains("DIAMOND")) {
            long i = Arrays.stream(ev.getPlayer().getInventory().getArmorContents())
                    .filter(itemStack -> itemStack.getType().name().contains("DIAMOND"))
                    .count();

            if (i >= EnhancedDiamondLimit.levels.get(ev.getPlayer().getUniqueId()).getDiamondPieces()) {
                ev.setCancelled(true);
                ev.getPlayer().sendMessage(Plugin.getPrefix() + "§cVous ne pouvez pas porter plus de pièces en diamant !");
            }
        }
    }


    private int getOreRandom(double percentage) {
        if (this.getGame().getRandom().nextDouble() <= percentage)
            return 2;
        return 1;
    }

    private static void updateConfig(UUID uuid) {
        File file = new File(Plugin.getINSTANCE().getDataFolder(), "diamondLimitLevel.yml");
        YamlConfiguration yconfig = YamlConfiguration.loadConfiguration(file);

        yconfig.set(uuid.toString(), EnhancedDiamondLimit.levels.get(uuid).getLevel());

        try {
            yconfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static final class ChangeLevelGUI implements InventoryProvider {

        public static final SmartInventory INVENTORY = SmartInventory.builder()
                .id("lg_commands_enhanceddlimit_inv")
                .manager(Plugin.getINSTANCE().getInvManager())
                .provider(new ChangeLevelGUI())
                .size(4, 9)
                .title("§bChanger la limite de Diamants")
                .closeable(true)
                .build();

        private final WereWolfAPI game = Plugin.getINSTANCE().getGame();

        @Override
        public void init(Player player, InventoryContents inventoryContents) {
            final Pagination pagination = inventoryContents.pagination();
            pagination.setItemsPerPage(9 * 3);
        }

        @Override
        public void update(Player player, InventoryContents contents) {
            final Pagination pagination = contents.pagination();
            final SlotIterator playerIterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
            final List<ClickableItem> items = new ArrayList<>();

            for (int column = 0; column < 9; column++) {
                playerIterator.blacklist(3, column);
            }

            game.getPlayersWW()
                    .forEach(playerWW -> items.add(ClickableItem.of(this.getPlayerItem(playerWW),
                            ev -> {
                                LimitLevel limitLevel = EnhancedDiamondLimit.levels.get(playerWW.getUUID());
                                if (ev.isRightClick() && limitLevel.getLevel() != 1)
                                    EnhancedDiamondLimit.levels.put(playerWW.getUUID(), LimitLevel.getByLevel(limitLevel.getLevel() - 1));
                                else if (ev.isLeftClick() && limitLevel.getLevel() != 4)
                                    EnhancedDiamondLimit.levels.put(playerWW.getUUID(), LimitLevel.getByLevel(limitLevel.getLevel() + 1));

                                EnhancedDiamondLimit.updateConfig(playerWW.getUUID());
                            })));

            pagination.setItems(items.toArray(new ClickableItem[0]));

            if (!pagination.isFirst())
                contents.set(4, 3, ClickableItem.of(new ItemBuilder(Material.ARROW).setDisplayName("§bPrécédant").build(), onClick -> INVENTORY.open((Player) onClick.getWhoClicked(), pagination.previous().getPage()))); //previous page

            if (!pagination.isLast())
                contents.set(4, 5, ClickableItem.of(new ItemBuilder(Material.ARROW).setDisplayName("§bSuivant").build(), onClick -> INVENTORY.open((Player) onClick.getWhoClicked(), pagination.next().getPage()))); //next page

            pagination.addToIterator(playerIterator);
        }


        private ItemStack getPlayerItem(IPlayerWW playerWW) {
            LimitLevel limitLevel = EnhancedDiamondLimit.levels.get(playerWW.getUUID());
            ItemBuilder item = new ItemBuilder(Material.PAPER);
            List<String> lore = new ArrayList<>(Arrays.asList("§7Change la limite de Diamants de " + playerWW.getName() + ".",
                    "",
                    "§7Niveau Actuel : §b§l" + limitLevel.getLevel(),
                    "",
                    "§7Nombre de Diamants maximum : §3§l" + limitLevel.getDiamonds(),
                    "§7Nombre de Pièces en Diamant max : §3§l" + limitLevel.getDiamondPieces(),
                    "§7Probabilité de drop l'or doublé : §3§l" + (int) (limitLevel.getGoldBoostPercentage() * 100) + "%",
                    "§7Probabilité de drop le diamant doublé : §3§l" + (int) (limitLevel.getDiamondBoostPercentage() * 100) + "%",
                    "",
                    "§7>>Clic gauche pour augmenter de niveau",
                    "§7>>Clic droit pour diminuer de niveau"));


            item.setDisplayName("§b" + playerWW.getName());
            item.setLore(lore);

            return item.build();
        }
    }


    @Getter
    public static class LimitLevel {

        private final int level;
        private final int diamonds;
        private final int diamondPieces;
        private final double goldBoostPercentage;
        private final double diamondBoostPercentage;

        public LimitLevel(int level, int diamonds, int diamondPieces, double goldBoostPercentage, double diamondBoostPercentage) {
            this.level = level;
            this.diamonds = diamonds;
            this.diamondPieces = diamondPieces;
            this.goldBoostPercentage = goldBoostPercentage;
            this.diamondBoostPercentage = diamondBoostPercentage;
        }

        public LimitLevel(int level, int diamonds, int diamondPieces) {
            this.level = level;
            this.diamonds = diamonds;
            this.diamondPieces = diamondPieces;
            this.goldBoostPercentage = -1.0D;
            this.diamondBoostPercentage = -1.0D;
        }


        public static LimitLevel getByLevel(int level) {
            switch (level) {
                case 1:
                    return LEVEL_1;
                case 2:
                    return LEVEL_2;
                case 3:
                    return LEVEL_3;
                case 4:
                    return LEVEL_4;
                default:
                    return null;
            }
        }

        public final static LimitLevel LEVEL_1 = new LimitLevel(1, 13, 1);
        public final static LimitLevel LEVEL_2 = new LimitLevel(2, 17, 2);
        public final static LimitLevel LEVEL_3 = new LimitLevel(3, 22, 3, 0.05D, 0.05D);
        public final static LimitLevel LEVEL_4 = new LimitLevel(4, 29, 4, 0.10D, 0.10D);
    }
}