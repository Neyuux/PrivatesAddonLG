package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.roles.CursedWerewolf;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Camp;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.AuraModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

@RoleCommand(key="privatesaddon.roles.cursed_werewolf.command", roleKeys={"privatesaddon.roles.cursed_werewolf.display"}, requiredPower=true, argNumbers={1})
public class CurseCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {
        UUID uuid = playerWW.getUUID();
        CursedWerewolf role = (CursedWerewolf) playerWW.getRole();
        Player player = Bukkit.getPlayer(uuid);
        Player playerArg = Bukkit.getPlayer(args[0]);
        if (playerArg == null) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur n'est pas en ligne !");
            return;
        }
        UUID argUUID = playerArg.getUniqueId();
        IPlayerWW playerWW1 = game.getPlayerWW(argUUID).orElse(null);
        if (argUUID.equals(uuid)) {
            playerArg.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVous ne pouvez pas vous maudire vous même !");
            return;
        }
        if (playerWW1 == null || !playerWW1.isState(StatePlayer.ALIVE)) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur est dans le couloir de la Mort ou est déjà Mort.");
            return;
        }
        role.setPower(false);
        /*TODO BeginSniffEvent beginSniffEvent = new BeginSniffEvent(playerWW, playerWW1);
        Bukkit.getPluginManager().callEvent(beginSniffEvent);
        if (beginSniffEvent.isCancelled()) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVotre pouvoir a été annulé.");
            return;
        }*/
        role.clearAffectedPlayer();
        role.addAffectedPlayer(playerWW1);
        player.sendMessage(Plugin.getPrefix() + "§fVous avez §cmaudit §b§l" + playerWW1.getName() + " §f!");

        IPlayerWW randomLG = Utils.autoSelect(game, playerWW);
        int max = 30;

        while ((!randomLG.getRole().isWereWolf() || randomLG.getRole().isInfected()) && max > 0) {
            randomLG = Utils.autoSelect(game, playerWW);
            max--;
        }

        playerWW1.getRole().setDisplayRole(randomLG.getRole().getKey());
        playerWW1.getRole().setDisplayCamp(Camp.WEREWOLF.getKey());
        playerWW1.getRole().addAuraModifier(new AuraModifier(role.getKey(), Aura.DARK, 20, true));
    }
}