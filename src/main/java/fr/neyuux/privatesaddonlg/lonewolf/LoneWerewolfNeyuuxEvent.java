package fr.neyuux.privatesaddonlg.lonewolf;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LoneWerewolfNeyuuxEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private boolean cancel = false;

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancelled) {
        this.cancel = cancelled;
    }
}
