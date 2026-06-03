package me.tecspace.skworldedit.api.utils;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public final class ExprUtils {

    public static double getSingle(@Nullable Expression<Double> expr, Event event, double defaultValue) {
        if (expr == null) return defaultValue;
        Double val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    public static int getSingle(@Nullable Expression<Integer> expr, Event event, int defaultValue) {
        if (expr == null) return defaultValue;
        Integer val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    public static boolean getSingle(@Nullable Expression<Boolean> expr, Event event, boolean defaultValue) {
        if (expr == null) return defaultValue;
        Boolean val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    public static <T> @Nullable T getSingle(@Nullable Expression<T> expr, Event event) {
        if (expr == null) return null;
        return expr.getSingle(event);
    }

    public static <T> T getSingle(@Nullable Expression<T> expr, Event event, T defaultValue) {
        if (expr == null) return defaultValue;
        T val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }
}
