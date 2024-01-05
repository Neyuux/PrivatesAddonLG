package fr.neyuux.privatesaddonlg.roles;

import com.avaje.ebean.annotation.UpdateMode;
import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.permissions.UpdateModeratorNameTagEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleNeutral;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;


@Role(key = "privatesaddon.roles.omniscient.display",
        defaultAura = Aura.NEUTRAL,
        category = Category.NEUTRAL,
        attributes = RoleAttribute.NEUTRAL)
public class Omniscient extends RoleNeutral {

    public Omniscient(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @Override
    public @NotNull String getDescription() {

        return new DescriptionBuilder(game, this)
                .setDescription("privatesaddon.roles.omniscient.description")
                .setItems(game.translate("privatesaddon.roles.omniscient.items"))
                .setCommand("privatesaddon.roles.omniscient.command")
                .build();
    }


    @Override
    public void recoverPower() {

    }


    @Override
    public void recoverPotionEffect() {

    }

    @Override
    public void disableAbilitiesRole() {
        Plugin.getINSTANCE().getGame().getPlayersWW()
                .forEach(playerWW -> Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(playerWW)));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNameTagUpdate(UpdatePlayerNameTagEvent ev) {
        Player player = Bukkit.getPlayer(ev.getPlayerUUID());
        Player target = Bukkit.getPlayer(ev.getTargetUUID());

        if (player == null || target == null)
            return;

        Scoreboard scoreboard = target.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        StringBuilder prefix = new StringBuilder(ev.getPrefix());
        StringBuilder suffix = new StringBuilder(ev.getSuffix());

        if (ev.isTabVisibility()) {
            VersionUtils.getVersionUtils().showPlayer(target, player);
        } else {
            VersionUtils.getVersionUtils().hidePlayer(target, player);
        }

        if (team != null) {
            game.getPlayerWW(target.getUniqueId()).ifPresent(targetWW -> {

                game.getPlayerWW(player.getUniqueId())
                        .ifPresent(playerWW -> {
                            UpdateModeratorNameTagEvent modNameTag = new UpdateModeratorNameTagEvent(player.getUniqueId());
                            Bukkit.getPluginManager().callEvent(modNameTag);

                            prefix.append(modNameTag.getPrefix());

                            suffix.append(modNameTag.getSuffix());

                            if(playerWW.isState(StatePlayer.ALIVE)){
                                prefix.append(targetWW.getColor(playerWW));
                            }
                        });

            });

            String string1 = suffix.toString();
            team.setSuffix(string1.substring(0, Math.min(16, string1.length())));
            String string2 = prefix.toString();
            team.setPrefix(string2.substring(Math.max(string2.length() - 16, 0)));
            VersionUtils.getVersionUtils().setTeamNameTagVisibility(team, ev.isVisibility());
        }
    }
}