package me.tecspace.skriptworldedit.api.lang;

import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link AsyncEffect} with the option to opt out of async execution by calling {@link #setAsync(boolean)}
 */
public abstract class ConditionalAsyncEffect extends AsyncEffect {

    private boolean async = true;

    /**
     * Whether this effect should run asynchronously
     */
    public void setAsync(boolean async) {
        getParser().setHasDelayBefore(Kleenean.get(async));
        this.async = async;
    }

    public boolean isAsync() {
        return this.async;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        if (isAsync()) {
            super.walk(event);
            return null;
        } else {
            execute(event);
            return getNext();
        }
    }
}
