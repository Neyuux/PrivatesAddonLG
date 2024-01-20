package fr.neyuux.privatesaddonlg.configs;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.events.game.life_cycle.DeathItemsEvent;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.assassin_drop_nerf.name", defaultValue = true, meetUpValue = true, loreKey = "privatesaddon.configurations.assassin_drop_nerf.description"))
public class AssassinDropNerf extends ListenerWerewolf {

    public AssassinDropNerf(WereWolfAPI game) {
        super(game);
    }

    public void register(boolean isActive) {
        super.register(isActive);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDropAssassin(DeathItemsEvent ev) {
        if (!Plugin.getINSTANCE().isLoaded())
            return;

        IConfiguration config = Plugin.getINSTANCE().getGame().getConfig();
        boolean chestplate = false;

        for (ItemStack item : ev.getItems()) {

            if (item == null || item.getType() == Material.AIR)
                return;

            if (item.getType().name().endsWith("HELMET") || item.getType().name().endsWith("CHESTPLATE") || item.getType().name().endsWith("LEGGINGS") || item.getType().name().endsWith("BOOTS")) {

                if (item.getType().name().startsWith("IRON")) {

                    if (item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) > config.getLimitProtectionIron()) {
                        if (!chestplate && item.getType() == Material.IRON_CHESTPLATE) {
                            chestplate = true;

                        } else {
                            item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
                            item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, config.getLimitProtectionIron());
                        }
                    }

                } else if (item.getType().name().startsWith("DIAMOND")) {

                    if (item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) > config.getLimitProtectionDiamond()) {

                        if (!chestplate && item.getType() == Material.DIAMOND_CHESTPLATE) {
                            chestplate = true;

                        } else {
                            item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
                            item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, config.getLimitProtectionDiamond());
                        }
                    }
                }
            }
        }
    }
}
