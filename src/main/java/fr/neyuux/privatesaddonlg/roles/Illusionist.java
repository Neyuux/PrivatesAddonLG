package fr.neyuux.privatesaddonlg.roles;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.events.roles.illusionist.IllusionistAddPlayerOnWerewolfListEvent;
import fr.ph1lou.werewolfapi.events.roles.illusionist.IllusionistGetNamesEvent;
import fr.ph1lou.werewolfapi.events.werewolf.AppearInWereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.NewWereWolfEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

@Role(
        key = "privates.roles.illusionist.display",
        category = Category.VILLAGER,
        attributes = {RoleAttribute.VILLAGER}
)
public class Illusionist extends RoleVillage implements IPower, IAffectedPlayers {
    private boolean power = true;
    private boolean wait = false;
    private IPlayerWW playerWW;

    public Illusionist(WereWolfAPI game, IPlayerWW playerWW) {
        super(game, playerWW);
    }

    @NotNull
    public String getDescription() {
        return (new DescriptionBuilder(this.game, this)).setDescription(this.game.translate("werewolf.roles.illusionist.description", new Formatter[0])).setPower(this.game.translate("werewolf.roles.illusionist.power", new Formatter[0])).setCommand(this.game.translate(this.hasPower() ? "werewolf.roles.illusionist.activate" : "werewolf.roles.illusionist.already_activate", new Formatter[0])).build();
    }

    @EventHandler
    public void onFinalDeathEvent(FinalDeathEvent event) {
        if (this.isAbilityEnabled()) {
            if (this.getPlayerWW().isState(StatePlayer.ALIVE)) {
                if (this.isWait()) {

                    this.setWait(false);
                    List<IPlayerWW> playersWW = this.game.getPlayersWW()
                            .stream()
                            .filter((playerWW1) -> !playerWW1.equals(this.getPlayerWW()) &&
                                    playerWW1.isState(StatePlayer.ALIVE) &&
                                    !playerWW1.getRole().isWereWolf())
                            .collect(Collectors.toList());

                    if (!playersWW.isEmpty()) {
                        Collections.shuffle(playersWW, this.game.getRandom());
                        IPlayerWW playerWW = playersWW.get(0);

                        IllusionistAddPlayerOnWerewolfListEvent illusionistAddPlayerOnWerewolfListEvent = new IllusionistAddPlayerOnWerewolfListEvent(this.getPlayerWW(), playerWW);
                        Bukkit.getPluginManager().callEvent(illusionistAddPlayerOnWerewolfListEvent);

                        if (illusionistAddPlayerOnWerewolfListEvent.isCancelled()) {
                            this.getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.check.cancel", new Formatter[0]);
                        } else {
                            this.addAffectedPlayer(playerWW);
                            Bukkit.getPluginManager().callEvent(new NewWereWolfEvent(playerWW));

                            BukkitUtils.scheduleSyncDelayedTask(this.game, () -> {
                                if (this.getPlayerWW().isState(StatePlayer.ALIVE)) {

                                    playerWW.sendMessageWithKey("werewolf.prefix.green", "werewolf.roles.illusionist.reveal", new Formatter[0]);

                                    List<IPlayerWW> players1WW = this.game.getPlayersWW()
                                            .stream()
                                            .filter((playerWW1) -> !playerWW1.equals(this.getPlayerWW()) &&
                                                    !playerWW1.equals(playerWW) &&
                                                    playerWW1.isState(StatePlayer.ALIVE))
                                            .collect(Collectors.toList());

                                    if (players1WW.size() >= 2) {

                                        Collections.shuffle(players1WW, this.game.getRandom());
                                        List<IPlayerWW> finalPlayersWW = new ArrayList<>(Arrays.asList(playerWW, players1WW.get(0), players1WW.get(1)));
                                        Collections.shuffle(finalPlayersWW, this.game.getRandom());

                                        IllusionistGetNamesEvent illusionistGetNamesEvent = new IllusionistGetNamesEvent(this.getPlayerWW(), new HashSet(finalPlayersWW));
                                        Bukkit.getPluginManager().callEvent(illusionistGetNamesEvent);

                                        if (illusionistGetNamesEvent.isCancelled())
                                            this.getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.check.cancel", new Formatter[0]);
                                        else
                                            this.getPlayerWW().sendMessageWithKey("werewolf.prefix.green", "werewolf.roles.illusionist.reveal_pseudos", Formatter.format("&names&", finalPlayersWW.stream().map(IPlayerWW::getName).collect(Collectors.joining(", "))));
                                    }
                                }
                            }, 1200L);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWerewolfListRequest(AppearInWereWolfListEvent event) {
        if (this.playerWW != null)
            if (event.getPlayerUUID().equals(this.playerWW.getUUID()))
                event.setAppear(true);
    }

    public void recoverPower() {
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean hasPower() {
        return this.power;
    }

    public void addAffectedPlayer(IPlayerWW iPlayerWW) {
        this.playerWW = iPlayerWW;
    }

    public void removeAffectedPlayer(IPlayerWW iPlayerWW) {
        if (iPlayerWW.equals(this.playerWW)) {
            this.playerWW = null;
        }

    }

    public void clearAffectedPlayer() {
        this.playerWW = null;
    }

    public List<? extends IPlayerWW> getAffectedPlayers() {
        return this.playerWW == null ? Collections.emptyList() : Collections.singletonList(this.playerWW);
    }

    public boolean isWait() {
        return this.wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }
}