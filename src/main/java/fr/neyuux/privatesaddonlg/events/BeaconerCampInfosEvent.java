package fr.neyuux.privatesaddonlg.events;

import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsEvent;
import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsExtraInfo;
import fr.ph1lou.werewolfapi.events.roles.SelectionEvent;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import lombok.Setter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@StatisticsEvent(key = "privatesaddon.beaconer_infos_camp")
public class BeaconerCampInfosEvent extends SelectionEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Setter
    private String camp;

    public BeaconerCampInfosEvent(IPlayerWW playerWW, IPlayerWW targetWW, String camp) {
        super(playerWW, targetWW);
        this.camp = camp;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @StatisticsExtraInfo
    public String getCamp() {
        return this.camp;
    }
}
