package fr.neyuux.privatesaddonlg.configurations;

import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configs.pearldroprate.name", defaultValue = true, meetUpValue = true), configValues = {@IntValue(key = "privatesaddon.configs.pearldroprate.configurations.percentage", defaultValue = 10, meetUpValue = 10, step = 5, item = UniversalMaterial.ENDER_PEARL)})
public class PearlDroprateConfig extends ListenerWerewolf {

    public PearlDroprateConfig(WereWolfAPI game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDropPearl(EntityDeathEvent ev) {
        WereWolfAPI game = getGame();
        List<ItemStack> drops = ev.getDrops();

        drops.removeIf(item -> item.getType() == Material.ENDER_PEARL);

        if (game.getRandom().nextDouble() <= this.getGame().getConfig().getValue("privatesaddon.configs.pearldroprate.configurations.percentage") / 100.0)
            drops.add(new ItemStack(Material.ENDER_PEARL));
    }

}
