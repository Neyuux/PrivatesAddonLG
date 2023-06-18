package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.utils.EnchantmentEvent;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class RoleBuffListener implements Listener {

    @EventHandler
    public void onBuffSharpnessCharmeuse(EnchantmentEvent ev) {
        final IPlayerWW playerWW = ev.getPlayerWW();

        if (!playerWW.isState(StatePlayer.ALIVE))
            return;

        final IConfiguration config = Plugin.getINSTANCE().getGame().getConfig();
        final Map<Enchantment, Integer> enchants = ev.getEnchants();

        if (playerWW.getRole().isKey("werewolf.roles.charmer.display"))
            if (enchants.containsKey(Enchantment.DAMAGE_ALL)) {
                if (ev.getItem().getType() == Material.DIAMOND_SWORD) {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessDiamond() + 1));
                } else {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessIron() + 1));
                }
            }
    }

}
