package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.roles.fox.BeginSniffEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.ILimitedUse;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IProgress;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RoleCommand(key="privatesaddon.roles.fox.command", roleKeys={"privatesaddon.roles.fox.display"}, requiredPower=true, argNumbers={1})
public class FlairerCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {
        UUID uuid = playerWW.getUUID();
        IRole fox = playerWW.getRole();
        Player player = Bukkit.getPlayer(uuid);
        Player playerArg = Bukkit.getPlayer(args[0]);
        if (playerArg == null) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur n'est pas en ligne !");
            return;
        }
        UUID argUUID = playerArg.getUniqueId();
        IPlayerWW playerWW1 = game.getPlayerWW(argUUID).orElse(null);
        if (argUUID.equals(uuid)) {
            playerArg.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVous ne pouvez pas vous flairer vous même !");
            return;
        }
        if (playerWW1 == null || !playerWW1.isState(StatePlayer.ALIVE)) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur n'existe pas, est dans le couloir de la Mort ou est déjà Mort.");
            return;
        }
        if (((ILimitedUse) fox).getUse() >= game.getConfig().getValue("privatesaddon.roles.fox.configurations.fox_smell_number")) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVous ne pouvez plus flairer de joueurs !");
            return;
        }
        Location location = playerWW.getLocation();
        Location locationTarget = playerArg.getLocation();
        if (location.getWorld() == playerArg.getWorld()) {
            if (location.distance(locationTarget) > (double)game.getConfig().getValue("privatesaddon.roles.fox.configurations.distance")) {
                player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVous n'êtes pas assez proche de ce joueur !");
                return;
            }
        } else {
            return;
        }
        ((IPower) fox).setPower(false);
        ((ILimitedUse) fox).setUse(((ILimitedUse) fox).getUse() + 1);
        BeginSniffEvent beginSniffEvent = new BeginSniffEvent(playerWW, playerWW1);
        Bukkit.getPluginManager().callEvent(beginSniffEvent);
        if (beginSniffEvent.isCancelled()) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVotre pouvoir a été annulé.");
            return;
        }
        ((IAffectedPlayers) fox).clearAffectedPlayer();
        ((IAffectedPlayers) fox).addAffectedPlayer(playerWW1);
        ((IProgress) fox).setProgress(0.0f);
        player.sendMessage(Plugin.getPrefix() + "§eVous avez commencé à flairer §l" + playerWW1.getName() + " §e!");
    }
}