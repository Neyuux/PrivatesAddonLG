package fr.neyuux.privatesaddonlg.groupscheck;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.AdminCommand;
import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import org.bukkit.entity.Player;

@AdminCommand(key = "privatesaddon.commands.admin.groupchecks.command", descriptionKey = "privatesaddon.commands.admin.groupchecks.description", argNumbers = {1}, statesGame = {StateGame.GAME})
public class CommandGroupChecks implements ICommand {
    public void execute(WereWolfAPI game, Player player, String[] args) {
        IPlayerWW playerWW1 = game.getPlayersWW().stream().filter(iPlayerWW -> iPlayerWW.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);

        if (playerWW1 == null) {
            player.sendMessage(Plugin.getPrefix() + "§cJoueur introuvable.");
            return;
        }

        if (!Plugin.getINSTANCE().getGroupsWarning().containsKey(playerWW1.getUUID()))
            return;

        player.sendMessage(Plugin.getPrefix() + "§cNombres d'irrespects des groupes : §b§l" + Plugin.getINSTANCE().getGroupsWarning().get(playerWW1.getUUID()) + " §b§o(§l" + Plugin.getINSTANCE().getGroupsWarningRatio(playerWW1.getUUID()) + "§b§o / minute)");
    }
}
