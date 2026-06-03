package me.tecspace.skworldedit.api.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import me.tecspace.skworldedit.SkWorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public final class RunnableUtils {

    public static void run(boolean async, Runnable runnable) {
        run(async, runnable, null);
    }

    public static void run(boolean async, Runnable runnable, @Nullable Runnable callback) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(SkWorldEdit.getInstance(), () -> {
                runnable.run();
                if (callback != null)
                    Bukkit.getScheduler().runTask(SkWorldEdit.getInstance(), callback);
            });
        } else {
            runnable.run();
            if (callback != null) callback.run();
        }
    }



    //
    public static void run(Event e, boolean async, boolean delay, @Nullable TriggerItem triggerItem, Runnable runnable) {

        if (!async) {
            TriggerItem.walk(triggerItem, e);
            runnable.run();

        } else if (!delay) {
            Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), runnable);
            TriggerItem.walk(triggerItem, e);

        } else {

            Object localVars = Variables.removeLocals(e); // Back up local variables

            if (!Skript.getInstance().isEnabled()) // See https://github.com/SkriptLang/Skript/issues/3702
                return;

            Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
                Delay.addDelayedEvent(e); // Mark this event as delayed
                // Re-set local variables
                if (localVars != null)
                    Variables.setLocalVariables(e, localVars);

                runnable.run();
                //execute(e); // Execute this effect

                if (triggerItem != null) {
                    Bukkit.getScheduler().runTask(Skript.getInstance(), () -> { // Walk to next item synchronously

                        TriggerItem.walk(triggerItem, e);

                        Variables.removeLocals(e); // Clean up local vars, we may be exiting now
                    });
                } else {
                    Variables.removeLocals(e);
                }
            });
        }
    }
}
