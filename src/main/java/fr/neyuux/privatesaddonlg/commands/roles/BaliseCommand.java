package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.roles.Beaconer;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@RoleCommand(key="privatesaddon.roles.beaconer.command", roleKeys={"privatesaddon.roles.beaconer.display"}, requiredPower=true, argNumbers={0})
public class BaliseCommand implements ICommandRole {

    private static final List<Beaconer.BeaconType> beaconsOrder = Arrays.asList(Beaconer.BeaconType.CAMP, Beaconer.BeaconType.EFFECTS, null);

    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {
        Beaconer beaconer = (Beaconer) playerWW.getRole();
        @Nullable Beaconer.BeaconType beacon = beaconer.getNextBeacon();

        if (beacon == Beaconer.BeaconType.CAMP)
            beaconer.setNextBeacon(Beaconer.BeaconType.EFFECTS);
        else if (beacon == Beaconer.BeaconType.EFFECTS)
            beaconer.setNextBeacon(null);
        else if (beacon == null)
            beaconer.setNextBeacon(Beaconer.BeaconType.CAMP);

        if (beaconer.getNextBeacon() != null)
            playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous avez équipé votre balise \"§b§l" + beaconer.getNextBeacon().getName() + "§f\". Faites un clic droit sur un joueur pour la poser. Refaites la commande pour changer de balise."));
        else
            playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous avez §cdéséquipé §fvotre balise."));

        Player player = Bukkit.getPlayer(playerWW.getUUID());

        if (player != null)
            Sound.ANVIL_LAND.play(player, 4f, 1.5f);
    }


    private Beaconer.BeaconType getNextBeacon(Beaconer.BeaconType beaconType, Beaconer beaconer) {
        int index = beaconsOrder.indexOf(beaconType);
        int beacon = (index == beaconsOrder.size() - 1 ? 0 : ++index);

    }
}
