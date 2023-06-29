package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.annotations.ConfigurationBasic;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.vote.VoteResultEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.versions.VersionUtils_1_8;
import fr.ph1lou.werewolfapi.vote.IVoteManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

@Configuration(config = @ConfigurationBasic(key = "privatesaddon.configurations.old_votes.name", loreKey = "privatesaddon.configurations.old_votes.description", defaultValue = true, meetUpValue = true), configValues = {@IntValue(key = "privatesaddon.configurations.old_votes.configurations.healtimer", defaultValue = 60, meetUpValue = 50, step = 5, item = UniversalMaterial.ARROW)})
public class VoteModifier extends ListenerWerewolf {

    public VoteModifier(WereWolfAPI game) {
        super(game);
    }


    @EventHandler
    public void onVoteResult(VoteResultEvent ev) {
        ev.setCancelled(true);

        this.showResultVote(ev.getPlayerWW());
    }

    public void showResultVote(@Nullable IPlayerWW playerWW) {
        WereWolfAPI game = Plugin.getINSTANCE().getGame();
        IVoteManager voteManager = game.getVoteManager();
        
        if (playerWW == null) {
            Bukkit.broadcastMessage(game.translate("werewolf.prefix.orange", "werewolf.configurations.vote.no_result", new Formatter[0]));
            return;
        }
        if (voteManager.getVotes(playerWW) < 3) {
            Bukkit.broadcastMessage(game.translate("werewolf.prefix.orange", "werewolf.configurations.vote.no_result_more_one", new Formatter[0]));
            return;
        }

        final double baseHealth = playerWW.getMaxHealth();

        new BukkitRunnable() {
            @Override
            public void run() {
                playerWW.addPlayerMaxHealth(2);

                if (playerWW.getMaxHealth() >= baseHealth) {
                    VersionUtils_1_8.getVersionUtils().setPlayerMaxHealth(Bukkit.getPlayer(playerWW.getUUID()), baseHealth);
                    cancel();
                }
            }
        }.runTaskTimer(Plugin.getINSTANCE(), 1L, 20L * game.getConfig().getValue("privatesaddon.configurations.old_votes.configurations.healtimer"));

        playerWW.removePlayerMaxHealth(baseHealth / 2.0D);

        Bukkit.broadcastMessage(game.translate("werewolf.prefix.yellow", "werewolf.configurations.vote.vote_result", Formatter.player(playerWW.getName()), Formatter.number(voteManager.getVotes(playerWW))));

        game.getPlayersWW().stream().filter(playerWW1 -> playerWW1.isState(StatePlayer.ALIVE)).forEach(playerWW1 -> {
            if (playerWW1.getLocation().getWorld() == playerWW.getLocation().getWorld()) {
                playerWW1.sendMessageWithKey("werewolf.prefix.yellow", "werewolf.configurations.vote.distance_voted", Formatter.number((int)playerWW1.getLocation().distance(playerWW.getLocation()) / 100 * 100 + 100));
            }
        });
    }
}
