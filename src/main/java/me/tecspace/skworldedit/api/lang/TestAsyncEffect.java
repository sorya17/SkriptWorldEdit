package me.tecspace.skworldedit.api.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link EffectSection} with the option to run the effect async by calling {@link #setAsync(boolean)} and option to decide whether to delay Skript with {@link #setDelayed(boolean)}.
 * Majority of Skript and Minecraft APIs are not thread-safe, so be careful.
 */
public abstract class TestAsyncEffect extends EffectSection {

    private boolean async = false;
    private boolean delayed = false;

    /**
     * @return Code to be run based on {@link #setAsync(boolean)} and {@link #setDelayed(boolean)}. null to skip entirely.
     */
    protected abstract @Nullable Runnable execute(Event event);

    /**
     * Whether the effect should run asynchronously
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return this.async;
    }

    /**
     * Whether the async effect should be delayed. Only does something when {@link #setAsync(boolean)} is true.
     */
    public void setDelayed(boolean delayed) {
        getParser().setHasDelayBefore(Kleenean.get(delayed));
        this.delayed = delayed;
    }

    public boolean isDelayed() {
        return this.delayed;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        Runnable operation = execute(event);
        if (operation == null) return getNext(); // skip and go to next skript item
        if (!isAsync()) {
            operation.run();
            return super.walk(event, false);
        } else {
            if (!isDelayed()) {
                // run async and restart skript immediately
                Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), operation);
                return super.walk(event, false);
            } else {
                // run async and restart skript afterwards
                this.executeDelayedAsync(event, operation);
                return null;
            }
        }
    }

    private void executeDelayedAsync(Event e, Runnable operation) {
        debug(e, true);

        Object localVars = Variables.removeLocals(e); // Back up local variables

        if (!Skript.getInstance().isEnabled()) // See https://github.com/SkriptLang/Skript/issues/3702
            return;

        Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
            Delay.addDelayedEvent(e); // Mark this event as delayed
            // Re-set local variables
            if (localVars != null)
                Variables.setLocalVariables(e, localVars);

            operation.run(); // run operation

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
