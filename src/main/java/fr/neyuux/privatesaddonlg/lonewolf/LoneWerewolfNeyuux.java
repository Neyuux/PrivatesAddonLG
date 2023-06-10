package fr.neyuux.privatesaddonlg.lonewolf;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.interfaces.ICamp;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.stream.Collectors;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.lone_wolf.name"), timers = {@Timer(key = "privatesaddon.configurations.lone_wolf.timer", defaultValue = 5400, meetUpValue = 1200, step = 30, onZero = LoneWerewolfNeyuuxEvent.class)})
public class LoneWerewolfNeyuux extends ListenerWerewolf {

    public LoneWerewolfNeyuux(WereWolfAPI main) {
        super(main);
    }

    @EventHandler
    public void onStart(StartEvent ev) {
        int divided = getGame().getConfig().getTimerValue("privatesaddon.configurations.lone_wolf.timer") / 5;
        getGame().getConfig().moveTimer("privatesaddon.configurations.lone_wolf.timer", getGame().getRandom().nextInt(divided * 2) - divided);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onStartDelay(LoneWerewolfNeyuuxEvent event) {
        WereWolfAPI game = getGame();
        List<IRole> roleWWs = game.getPlayersWW().stream().filter(playerWW -> playerWW.isState(StatePlayer.ALIVE)).map(IPlayerWW::getRole).filter(ICamp::isWereWolf).collect(Collectors.toList());
        if (roleWWs.isEmpty())
            return;
        IRole role = roleWWs.get((int)Math.floor(game.getRandom().nextDouble() * roleWWs.size()));
        if (event.isCancelled())
            return;

        role.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§cVous êtes le §4§lLoup Solitaire§c. A partir de maintenant, votre objectif est de gagner seul. Pour ce faire vous disposez de §44♥ §cbonus (15 max)."));

        if (role.getPlayerWW().getMaxHealth() < 30.0D)
            role.getPlayerWW().addPlayerMaxHealth(Math.max(0.0D, Math.min(8.0D, 30.0D - role.getPlayerWW().getMaxHealth())));
        role.setSolitary(true);
        register(false);
    }
}
