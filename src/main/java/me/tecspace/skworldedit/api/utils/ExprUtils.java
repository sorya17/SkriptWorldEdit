package me.tecspace.skworldedit.api.utils;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public final class ExprUtils {

    public static double getSingle(Event event, @Nullable Expression<Double> expr, double defaultValue) {
        if (expr == null) return defaultValue;
        Double val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    public static int getSingle(Event event, @Nullable Expression<Integer> expr, int defaultValue) {
        if (expr == null) return defaultValue;
        Integer val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    public static boolean getSingle(Event event, @Nullable Expression<Boolean> expr, boolean defaultValue) {
        if (expr == null) return defaultValue;
        Boolean val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }

    /**
     * If expr is null will return null else will get the object from the expression
     */
    public static <T> @Nullable T getSingle(Event event, @Nullable Expression<T> expr) {
        if (expr == null) return null;
        return expr.getSingle(event);
    }

    public static <T> T getSingle(Event event, @Nullable Expression<T> expr, T defaultValue) {
        if (expr == null) return defaultValue;
        T val = expr.getSingle(event);
        return (val != null) ? val : defaultValue;
    }
}
