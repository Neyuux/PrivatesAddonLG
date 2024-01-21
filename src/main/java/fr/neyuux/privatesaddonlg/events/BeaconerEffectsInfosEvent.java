package fr.neyuux.privatesaddonlg.events;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsEvent;
import fr.ph1lou.werewolfapi.annotations.statistics.StatisticsExtraInfo;
import fr.ph1lou.werewolfapi.events.roles.SelectionEvent;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

@StatisticsEvent(key = "privatesaddon.beaconer_infos_effects")
public class BeaconerEffectsInfosEvent extends SelectionEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Set<PotionEffectType> effects;

    public BeaconerEffectsInfosEvent(IPlayerWW playerWW, IPlayerWW targetWW, Set<PotionEffectType> effects) {
        super(playerWW, targetWW);
        this.effects = effects;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @StatisticsExtraInfo
    public String getExtraInfo() {
        return this.getEffects()
                .stream()
                .map(Plugin::translatePotionEffect)
                .collect(Collectors.joining(", "));
    }

    public Set<PotionEffectType> getEffects() {
        return this.effects;
    }
}
