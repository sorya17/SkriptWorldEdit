package me.tecspace.skriptworldedit.api.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Async implementation of {@link Section} with the option to opt out of async execution by calling {@link #setAsync(boolean)}
 */
public abstract class AsyncSection extends Section {

    private boolean async = true;

    protected abstract void execute(Event event);

    /**
     * Whether this section should run asynchronously
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return this.async;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (isAsync()) {
            executeAsync(event);
            return null;
        } else {
            execute(event);
            return super.walk(event, false);
        }
    }

    private void executeAsync(Event e) {
        debug(e, true);

        Object localVars = Variables.removeLocals(e); // Back up local variables

        if (!Skript.getInstance().isEnabled()) // See https://github.com/SkriptLang/Skript/issues/3702
            return;

        Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
            Delay.addDelayedEvent(e); // Mark this event as delayed
            // Re-set local variables
            if (localVars != null)
                Variables.setLocalVariables(e, localVars);

            execute(e); // Execute this effect

            if (getNext() != null) {
                Bukkit.getScheduler().runTask(Skript.getInstance(), () -> { // Walk to next item synchronously
                    Object timing = null;
                    if (SkriptTimings.enabled()) { // getTrigger call is not free, do it only if we must
                        Trigger trigger = getTrigger();
                        if (trigger != null) {
                            timing = SkriptTimings.start(trigger.getDebugLabel());
                        }
                    }

                    TriggerItem.walk(getNext(), e);

                    Variables.removeLocals(e); // Clean up local vars, we may be exiting now

                    SkriptTimings.stop(timing); // Stop timing if it was even started
                });
            } else {
                Variables.removeLocals(e);
            }
        });
    }
}

