package me.tecspace.skworldedit.api.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

/**
 * general implementation of {@link AsyncEffect}
 */
public class DelayedAsyncTrigger {

    /**
     * Runs {@code operation} asynchronously. Next trigger item will be ran
     * in main server thread, as if there had been a delay before.
     */
    public static void execute(Event event, TriggerItem item, Runnable operation) {

        Object localVars = Variables.removeLocals(event); // back up local variables

        if (!Skript.getInstance().isEnabled()) // see https://github.com/SkriptLang/Skript/issues/3702
            return;

        Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
            Delay.addDelayedEvent(event); // mark this event as delayed
            if (localVars != null)
                Variables.setLocalVariables(event, localVars); // restore local variables

            operation.run();

            if (item.getNext() != null) {
                Bukkit.getScheduler().runTask(Skript.getInstance(), () -> { // resume script on main thread

                    TriggerItem.walk(item.getNext(), event);

                    Variables.removeLocals(event); // clean up local variables
                });
            } else {
                Variables.removeLocals(event); // clean up even if there's nothing left to run
            }
        });
    }
}