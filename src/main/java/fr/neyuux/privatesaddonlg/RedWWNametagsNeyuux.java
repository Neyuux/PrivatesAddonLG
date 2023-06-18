package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.werewolf.AppearInWereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.RequestSeeWereWolfListEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;
import java.util.UUID;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.red_name_tag.name", defaultValue = true, meetUpValue = true, incompatibleConfigs = {"werewolf.configurations.red_name_tag.name"}))
public class RedWWNametagsNeyuux extends ListenerWerewolf {

    private final WereWolfAPI game;

    public RedWWNametagsNeyuux(WereWolfAPI game) {
        super(game);
        this.game = game;
    }

    public void register(boolean isActive) {
        super.register(isActive);
        Bukkit.getOnlinePlayers().forEach(player -> Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(player)));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWWNameTagUpdate(UpdatePlayerNameTagEvent ev) {
        Optional<IPlayerWW> optionalPlayer = this.game.getPlayerWW(ev.getPlayerUUID());
        Optional<IPlayerWW> optionalTarget = this.game.getPlayerWW(ev.getTargetUUID());

        if (!optionalPlayer.isPresent() || !optionalTarget.isPresent())
            return;

        IPlayerWW playerWW = optionalPlayer.get();
        IPlayerWW targetWW = optionalTarget.get();

        Player player = Bukkit.getPlayer(ev.getPlayerUUID());
        Player target = Bukkit.getPlayer(ev.getTargetUUID());

        if (player == null || target == null)
            return;

        Scoreboard scoreboard = target.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        StringBuilder sb = new StringBuilder(ev.getPrefix());

        if (ev.isTabVisibility()) {
            VersionUtils.getVersionUtils().showPlayer(target, player);
        } else {
            VersionUtils.getVersionUtils().hidePlayer(target, player);
        }

        if (team != null) {
            Bukkit.getScheduler().runTaskLater(Plugin.getINSTANCE(), () -> {
                UUID uuid1 = target.getUniqueId();
                RequestSeeWereWolfListEvent requestSeeWereWolfListEvent = new RequestSeeWereWolfListEvent(uuid1);
                Bukkit.getPluginManager().callEvent(requestSeeWereWolfListEvent);

                if (requestSeeWereWolfListEvent.isAccept()) {
                    AppearInWereWolfListEvent appearInWereWolfListEvent = new AppearInWereWolfListEvent(player.getUniqueId(), uuid1);
                    Bukkit.getPluginManager().callEvent(appearInWereWolfListEvent);

                    if ((appearInWereWolfListEvent.isAppear() || uuid1.equals(player.getUniqueId())) && this.game.getConfig().isConfigActive("privatesaddon.configurations.red_name_tag.name")) {
                        sb.append("§4");
                    }
                }

                if (!uuid1.equals(player.getUniqueId())) {
                    ChatColor color = targetWW.getColor(playerWW);
                    if (playerWW.isState(StatePlayer.ALIVE) && color != ChatColor.RESET) {
                        sb.append(color);
                    }
                }

                VersionUtils.getVersionUtils().setTeamNameTagVisibility(team, ev.isVisibility());

                String string1 = ev.getSuffix();
                team.setSuffix(string1.substring(0, Math.min(16, string1.length())));
                String string2 = sb.toString();
                team.setPrefix(string2.substring(Math.max(string2.length() - 16, 0)));
                target.setScoreboard(scoreboard);
            }, 1L);
        }
    }

}
