package fr.neyuux.privatesaddonlg.configurations;

import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configs.flintdroprate.name", defaultValue = true, meetUpValue = true), configValues = {@IntValue(key = "privatesaddon.configs.flintdroprate.configurations.percentage", defaultValue = 50, meetUpValue = 70, step = 5, item = UniversalMaterial.FLINT)})
public class FlintDroprateConfig extends ListenerWerewolf {

    public FlintDroprateConfig(WereWolfAPI game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreakGravel(BlockBreakEvent ev) {
        WereWolfAPI game = getGame();
        Block block = ev.getBlock();

        if (block.getType() == Material.GRAVEL) {
            World w = block.getWorld();

            ev.setCancelled(true);
            block.setType(Material.AIR, true);
            w.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

            if (game.getRandom().nextDouble() <= game.getConfig().getValue("privatesaddon.configs.flintdroprate.configurations.percentage") / 100.0)
                w.dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.FLINT));
        }

    }

}
