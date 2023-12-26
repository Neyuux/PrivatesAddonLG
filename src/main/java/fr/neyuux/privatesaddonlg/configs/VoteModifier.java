package fr.neyuux.privatesaddonlg.configs;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.vote.VoteResultEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.versions.VersionUtils_1_8;
import fr.ph1lou.werewolfapi.vote.IVoteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.old_votes.name", loreKey = "privatesaddon.configurations.old_votes.description", defaultValue = false, meetUpValue = false), configValues = {@IntValue(key = "privatesaddon.configurations.old_votes.configurations.healtimer", defaultValue = 60, meetUpValue = 50, step = 5, item = UniversalMaterial.ARROW)})
public class VoteModifier extends ListenerWerewolf {

    public VoteModifier(WereWolfAPI game) {
        super(game);
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoteResult(VoteResultEvent ev) {
        if (ev.isCancelled())
            return;

        this.showResultVote(ev.getPlayerWW());

        ev.setCancelled(true);
    }

    public void showResultVote(@Nullable IPlayerWW playerWW) {
        WereWolfAPI game = Plugin.getINSTANCE().getGame();
        IVoteManager voteManager = game.getVoteManager();

        if (playerWW == null) {
            Bukkit.broadcastMessage("§f[§6LG UHC§f] §eLe Vote n'a eu aucun résultat.");
            return;
        }
        if (voteManager.getVotes(playerWW) < 3) {
            Bukkit.broadcastMessage("§f[§6LG UHC§f] §eAucun joueur n'a été voté suffisamment de fois.");
            return;
        }

        Player player = Bukkit.getPlayer(playerWW.getUUID());
        final double baseHealth = playerWW.getMaxHealth();
        long delay = 20L * game.getConfig().getValue("privatesaddon.configurations.old_votes.configurations.healtimer");

        new BukkitRunnable() {
            @Override
            public void run() {
                playerWW.addPlayerMaxHealth(2);

                if (playerWW.getMaxHealth() >= baseHealth) {
                    VersionUtils_1_8.getVersionUtils().setPlayerMaxHealth(Bukkit.getPlayer(playerWW.getUUID()), baseHealth);
                    cancel();
                }
            }
        }.runTaskTimer(Plugin.getINSTANCE(), delay, delay);

        playerWW.removePlayerMaxHealth(baseHealth / 2.0D);
        if (player != null)
            player.damage(0);

        Bukkit.broadcastMessage("§f[§eLG UHC§f] §eLe Vote possède un résultat : §l" + playerWW.getName() + "§e. Il a obtenu §6§l" + voteManager.getVotes(playerWW) + "§e vote(s).");

        game.getPlayersWW().stream().filter(playerWW1 -> playerWW1.isState(StatePlayer.ALIVE)).forEach(playerWW1 -> {
            if (playerWW1.getLocation().getWorld() == playerWW.getLocation().getWorld()) {
                int distance = (int)playerWW1.getLocation().distance(playerWW.getLocation()) / 100 * 100 + 100;

                Bukkit.getPlayer(playerWW1.getUUID()).sendMessage("§f[§6LG UHC§f] §fIl se situe à §e" + distance + "§f blocs de vous. (100 blocs près)");
            }
        });
    }
}
