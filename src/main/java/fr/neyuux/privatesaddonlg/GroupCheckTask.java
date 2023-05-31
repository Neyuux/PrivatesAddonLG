package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Timer(decrement = true, key = "privatesaddon.timers.groupcheck.name", loreKey = "privatesaddon.timers.groupcheck.description"
, defaultValue = 60, meetUpValue = 60, decrementAfterRole = true, onZero = GroupCheckEvent.class)
public class GroupCheckTask extends ListenerWerewolf {

    private final WereWolfAPI game;

    public GroupCheckTask(WereWolfAPI game) {
        super(game);
        this.game = game;
    }

    @Getter
    private final HashMap<UUID, Integer> groupsWarning = new HashMap<>();

    @EventHandler
    public void onCheckEvent(GroupCheckEvent ev) {

        for (IPlayerWW playerWW : game.getPlayersWW()) {
            if (playerWW.isState(StatePlayer.ALIVE) && Bukkit.getOnlinePlayers().stream().anyMatch(player -> player.getUniqueId().equals(playerWW.getUUID()))) {

                Player player = Bukkit.getPlayer(playerWW.getUUID());
                UUID uuid = player.getUniqueId();
                boolean isSpying = player.isSneaking() && Bukkit.getOnlinePlayers().stream()
                        .noneMatch(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 16);

                if (isSpying || player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                    continue;

                long around = Bukkit.getOnlinePlayers().stream()
                        .filter(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 60 * 60)
                        .filter(player1 -> game.getPlayerWW(player1.getUniqueId()).isPresent() && game.getPlayerWW(player1.getUniqueId()).get().isState(StatePlayer.ALIVE))
                        .count();

                if (around > game.getGroup())
                    if (!this.groupsWarning.containsKey(uuid))
                        this.groupsWarning.put(uuid, 1);
                    else
                        this.groupsWarning.put(uuid, this.groupsWarning.get(uuid) + 1);

                Bukkit.getLogger().info(player.getName() + " a dépassé la limite des groupes ! (" + around + " au lieu de " + game.getGroup() + ")");
            }
        }


        this.addTimerValue(60);
    }

    @EventHandler
    public void onFinish(WinEvent ev) {
        Bukkit.broadcastMessage(Plugin.getPrefix() + "§cRésumé des analyses de groupes : ");

        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(this.groupsWarning.entrySet());
        list.sort(Map.Entry.comparingByValue());


        for (Map.Entry<UUID, Integer> entry : list)
            game.getPlayerWW(entry.getKey())
                    .ifPresent(iPlayerWW ->
                            Bukkit.broadcastMessage(" §c" + iPlayerWW.getName() + " : §b§l" + entry.getValue()));
    }


    private int getTimerValue() {
        return game.getConfig().getTimerValue("privatesaddon.timers.groupcheck.name");
   }

   private void addTimerValue(int added) {
       game.getConfig().moveTimer("privatesaddon.timers.groupcheck.name", added);
   }
}
