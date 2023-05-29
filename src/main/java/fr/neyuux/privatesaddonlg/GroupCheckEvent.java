package fr.neyuux.privatesaddonlg;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupCheckEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
