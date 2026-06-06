package me.tecspace.skworldedit.api.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.tecspace.skworldedit.SkWorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link Effect}.
 * you may use {@link #setAsync(boolean)} and {@link #setDelayed(boolean)} in {@code load}.
 * <p>
 * Skript code should be run before the lambda returned by {@code runnable}.
 * Majority of Minecraft APIs are not thread-safe, so be careful.
 */
public abstract class CondAsyncEffect extends Effect {

    private boolean async = false;
    private boolean delayed = false;

    /**
     * @return Code to be run. null to skip execution entirely.
     */
    protected abstract @Nullable Runnable runnable(Event event);

    @Override protected void execute(Event event) {
        throw new UnsupportedOperationException();
    }

    /**
     * Whether it should run asynchronously
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
        Runnable operation = runnable(event);
        if (operation == null) return getNext(); // skip and go to next skript item
        if (!isAsync()) {
            operation.run();
            return getNext();
        } else {
            if (!isDelayed()) {
                // run async and restart skript immediately
                Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), operation);
                return getNext();
            } else {
                // run async and restart skript afterwards
                DelayedAsyncTrigger.execute(event, this, operation);
                return null;
            }
        }
    }

    /**
     * Override init so we can add checks here
     */
    protected abstract boolean load(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult);

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!earlyInit()) return false;
        if (!load(exprs, matchedPattern, isDelayed, parseResult)) return false;
        return finalInit();
    }

    private boolean earlyInit() {
        return true;
    }

    private boolean finalInit() {
        boolean lazily = !isAsync();
        boolean delayed = isDelayed();

        if (!SkWorldEdit.UsesFastAsyncWorldEdit) {
            if (lazily) Skript.warning("'lazily' has no effect because FAWE is not installed. The effect will run lazily anyway.");
            if (delayed) Skript.warning("'and wait' has no effect because FAWE is not installed. The effect doesn't have any delay.");
        }

        if (lazily && delayed) {
            Skript.warning("'and wait' has no effect when 'lazily' is used. you should remove it.");
        }
        return true;
    }
}
