package fr.neyuux.privatesaddonlg.groupscheck;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
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

    @EventHandler
    public void onCheckEvent(GroupCheckEvent ev) {

        HashSet<Player> checkable = new HashSet<>();
        HashMap<UUID, Integer> groupsWarning = Plugin.getINSTANCE().getGroupsWarning();

        game.getPlayersWW().stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE) && Bukkit.getOnlinePlayers().stream().anyMatch(player -> player.getUniqueId().equals(playerWW.getUUID())))
                .filter(playerWW -> {
                    Player player = Bukkit.getPlayer(playerWW.getUUID());
                    boolean isSpying = player.isSneaking() && Bukkit.getOnlinePlayers().stream()
                            .noneMatch(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 14);

                    return !isSpying && !player.hasPotionEffect(PotionEffectType.INVISIBILITY);
                })
                .forEach(iPlayerWW -> checkable.add(Bukkit.getPlayer(iPlayerWW.getUUID())));

        for (Player player : checkable) {
            UUID uuid = player.getUniqueId();

            long around = checkable.stream()
                    .filter(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 50 * 50)
                    .filter(player1 -> game.getPlayerWW(player1.getUniqueId()).isPresent() && game.getPlayerWW(player1.getUniqueId()).get().isState(StatePlayer.ALIVE))
                    .count();


            if (around > game.getGroup()) {
                Bukkit.getLogger().info(player.getName() + " a dépassé la limite des groupes ! (" + around + " au lieu de " + game.getGroup() + ")");

                if (!groupsWarning.containsKey(uuid))
                    groupsWarning.put(uuid, 1);
                else
                    groupsWarning.put(uuid, groupsWarning.get(uuid) + 1);
            }
        }


        this.addTimerValue(60);
    }

    @EventHandler
    public void onFinish(WinEvent ev) {
        Bukkit.broadcastMessage(Plugin.getPrefix() + "§cRésumé des analyses de groupes : ");

        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(Plugin.getINSTANCE().getGroupsWarning().entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);


        for (Map.Entry<UUID, Integer> entry : list) {
            game.getPlayerWW(entry.getKey())
                    .ifPresent(iPlayerWW ->
                            Bukkit.broadcastMessage(" §c" + iPlayerWW.getName() + " : §b§l" + entry.getValue() + " §b§o(§l" + Plugin.getINSTANCE().getGroupsWarningRatio(entry.getKey()) + "§b§o / minute)"));
        }
    }


    private int getTimerValue() {
        return game.getConfig().getTimerValue("privatesaddon.timers.groupcheck.name");
   }

   private void addTimerValue(int added) {
       game.getConfig().moveTimer("privatesaddon.timers.groupcheck.name", added);
   }
}
