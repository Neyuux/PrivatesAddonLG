package fr.neyuux.privatesaddonlg.lonewolf;

import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.configs.LoneWolfEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.lone_wolf.name", incompatibleConfigs = "werewolf.configurations.lone_wolf.name"), timers = {@Timer(key = "privatesaddon.configurations.lone_wolf.timer.name", loreKey = "privatesaddon.configurations.lone_wolf.timer.description", defaultValue = 3900, meetUpValue = 600, step = 30, decrementAfterRole = true, onZero = LoneWerewolfNeyuuxEvent.class)})
public class LoneWerewolfNeyuux extends ListenerWerewolf {

    public LoneWerewolfNeyuux(WereWolfAPI main) {
        super(main);
    }

    @EventHandler
    public void onStart(StartEvent ev) {
        int divided = getGame().getConfig().getTimerValue("privatesaddon.configurations.lone_wolf.timer.name") / 5;
        getGame().getConfig().moveTimer("privatesaddon.configurations.lone_wolf.timer.name", getGame().getRandom().nextInt(divided * 2) - divided);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLoneWW(LoneWerewolfNeyuuxEvent event) {
        WereWolfAPI game = getGame();
        List<IRole> roleWWs = game.getPlayersWW()
                .stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .map(IPlayerWW::getRole)
                .filter(role -> role.isWereWolf() && !role.isNeutral())
                .collect(Collectors.toList());
        if (roleWWs.isEmpty())
            return;
        IRole role = roleWWs.get((int)Math.floor(game.getRandom().nextDouble() * roleWWs.size()));
        if (event.isCancelled())
            return;

        IPlayerWW playerWW = role.getPlayerWW();
        @Nullable Player player = Bukkit.getPlayer(playerWW.getUUID());
        LoneWolfEvent loneWolfEvent = new LoneWolfEvent(playerWW);
        Bukkit.getPluginManager().callEvent(loneWolfEvent);

        playerWW.sendMessageWithKey("privatesaddon.prefix.red", "privatesaddon.configurations.lone_wolf.message");
        if (player != null)
            Sound.WITHER_HURT.play(player, 5f, 0.80f);

        if (playerWW.getMaxHealth() < 30.0D)
            playerWW.addPlayerMaxHealth(Math.max(0.0D, Math.min(8.0D, 30.0D - playerWW.getMaxHealth())));
        role.setSolitary(true);
        register(false);
    }
}
