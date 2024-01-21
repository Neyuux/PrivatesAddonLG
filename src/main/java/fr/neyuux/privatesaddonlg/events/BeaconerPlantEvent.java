package fr.neyuux.privatesaddonlg.events;

import fr.neyuux.privatesaddonlg.roles.Beaconer;
import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsEvent;
import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsExtraInfo;
import fr.ph1lou.werewolfapi.events.roles.SelectionEvent;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@StatisticsEvent(key = "privatesaddon.beaconer_plant")
public class BeaconerPlantEvent extends SelectionEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Beaconer.BeaconType beacon;

    public BeaconerPlantEvent(IPlayerWW playerWW, IPlayerWW targetWW, Beaconer.BeaconType beacon) {
        super(playerWW, targetWW);
        this.beacon = beacon;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @StatisticsExtraInfo
    public String getBeaconType() {
        return this.beacon.getName();
    }
}
