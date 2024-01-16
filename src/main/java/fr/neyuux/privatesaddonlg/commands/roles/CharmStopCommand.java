package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.basekeys.LoverBase;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RoleCommand(key="privatesaddon.roles.charmer.charmstopcommand", roleKeys={"werewolf.roles.charmer.display"}, argNumbers={0})
public class CharmStopCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {

        if (!((IAffectedPlayers) playerWW.getRole()).getAffectedPlayers().isEmpty()) {
            playerWW.getLovers().stream()
                    .filter(iLover -> iLover.isKey(LoverBase.FAKE_LOVER))
                    .forEach(iLover -> {
                        iLover.getLovers().forEach(iPlayerWW -> {
                            if (!iPlayerWW.getUUID().equals(playerWW.getUUID())) {
                                Player player = Bukkit.getPlayer(iPlayerWW.getUUID());

                                player.sendMessage(Plugin.getPrefix() + "§dVotre Couple avec §5§l" + playerWW.getName() + " §détait §c§lFAUX§d ! Vous pouvez désormais vivre serainement.");
                                Sound.COW_HURT.play(player, 5f, 1.3f);
                            }
                            iPlayerWW.removeLover(iLover);
                        });

                        iLover.getLovers().clear();
                    });


            Bukkit.getPlayer(playerWW.getUUID()).sendMessage(Plugin.getPrefix() + "§dVous avez révélé votre identité à votre charmé.");
        }
   }
}