package fr.neyuux.privatesaddonlg.groupscheck;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.stream.Collectors;

@Timer(decrement = true, key = "privatesaddon.timers.groupcheck.name", loreKey = "privatesaddon.timers.groupcheck.description"
, defaultValue = 60, meetUpValue = 60, decrementAfterRole = true, onZero = GroupCheckEvent.class)
public class GroupCheckTask extends ListenerWerewolf {

    private final HashMap<IPlayerWW, Long> lastDamages = new HashMap<>();

    private final WereWolfAPI game;

    public GroupCheckTask(WereWolfAPI game) {
        super(game);
        this.game = game;
    }

    @EventHandler
    public void onCheckEvent(GroupCheckEvent ev) {


        HashMap<UUID, Integer> groupsWarning = Plugin.getINSTANCE().getGroupsWarning();
        HashSet<Player> checkable = game.getPlayersWW().stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE) && Bukkit.getOnlinePlayers().stream().anyMatch(player -> player.getUniqueId().equals(playerWW.getUUID())))
                .filter(playerWW -> {
                    Player player = Bukkit.getPlayer(playerWW.getUUID());
                    boolean isSpying = player.isSneaking() && Bukkit.getOnlinePlayers().stream()
                            .noneMatch(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 14);

                    return !isSpying && !player.hasPotionEffect(PotionEffectType.INVISIBILITY);
                })
                .filter(playerWW -> !this.lastDamages.containsKey(playerWW) || System.currentTimeMillis() - this.lastDamages.get(playerWW) <= 30000)
                .map(iPlayerWW -> Bukkit.getPlayer(iPlayerWW.getUUID())).collect(Collectors.toCollection(HashSet::new));

        for (Player player : checkable) {
            UUID uuid = player.getUniqueId();

            long around = checkable.stream()
                    .filter(player1 -> player1.getLocation().distanceSquared(player.getLocation()) <= 50 * 50 &&
                            game.getPlayerWW(player1.getUniqueId()).isPresent() &&
                            game.getPlayerWW(player1.getUniqueId()).get().isState(StatePlayer.ALIVE))
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

    @EventHandler
    private void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        Player striker;
        if (!this.game.isState(StateGame.GAME)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getEntity();
        if (!(event.getDamager() instanceof Player)) {
            if (!(event.getDamager() instanceof Arrow)) {
                return;
            }
            ProjectileSource shooter = ((Arrow)event.getDamager()).getShooter();
            if (!(shooter instanceof Player)) {
                return;
            }
            striker = (Player)shooter;
        } else {
            striker = (Player)event.getDamager();

            if (!striker.getItemInHand().getType().name().endsWith("SWORD"))
                return;
        }


        this.game.getPlayerWW(player.getUniqueId()).ifPresent(playerWW -> this.game.getPlayerWW(striker.getUniqueId()).ifPresent(strikerWW ->
                lastDamages.put(playerWW, System.currentTimeMillis())));
    }


    private int getTimerValue() {
        return game.getConfig().getTimerValue("privatesaddon.timers.groupcheck.name");
   }

   private void addTimerValue(int added) {
       game.getConfig().moveTimer("privatesaddon.timers.groupcheck.name", added);
   }
}
