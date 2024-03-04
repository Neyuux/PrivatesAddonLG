package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.roles.Illusionist;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.events.roles.illusionist.IllusionistActivatePowerEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@RoleCommand(key="privatesaddon.roles.illusionist.command", roleKeys={"privatesaddon.roles.illusionist.display"}, argNumbers={0}, requiredPower = true)
public class ActiverCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {

        Illusionist illusionist = (Illusionist)playerWW.getRole();
        illusionist.setPower(false);
        IllusionistActivatePowerEvent event = new IllusionistActivatePowerEvent(playerWW);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            playerWW.sendMessage(new TextComponent(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVotre pouvoir a été annulé."));
            return;
        }
        illusionist.setWait(true);
        playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVotre pouvoir a été §aactivé§f ! Il s'activera à la prochaine mort."));
        Sound.LEVEL_UP.play(Bukkit.getPlayer(playerWW.getUUID()), 8f, 1.8f);
    }
}