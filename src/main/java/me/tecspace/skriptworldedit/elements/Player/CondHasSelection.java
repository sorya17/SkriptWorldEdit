package me.tecspace.skriptworldedit.elements.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import me.tecspace.skriptworldedit.api.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Player - Has Selection")
@Description("""
        Check whether a player has a selection (with their wand, or by command).
        Including 'complete' will also check whether the selection is a full usable region.
        """)
@Example("""
        if {_player} doesn't have a complete region selected:
            send "You need to select a region!" to {_player}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class CondHasSelection extends PropertyCondition<Player> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.CONDITION,
                infoBuilder(
                        CondHasSelection.class,
                        PropertyType.HAVE,
                        "[a] [:complete] [region] (selected|selection[s])",
                        "players"
                )
                        .supplier(CondHasSelection::new)
                        .build()
        );
    }

    private boolean complete;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        complete = parseResult.hasTag("complete");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public boolean check(Player player) {
        LocalSession session = PlayerUtils.getSession(player);
        if (session.getSelectionWorld() == null) return false;
        if (!complete) return true;
        try {
            session.getSelection(session.getSelectionWorld());
            return true;
        } catch (IncompleteRegionException e) {
            return false;
        }
    }

    @Override
    public String getPropertyName() {
        return "has worldedit selection";
    }
}