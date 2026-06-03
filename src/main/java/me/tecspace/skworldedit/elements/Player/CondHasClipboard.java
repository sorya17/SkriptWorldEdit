package me.tecspace.skworldedit.elements.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import me.tecspace.skworldedit.api.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Player - Has Clipboard")
@Description("Check whether a player has a clipboard.")
@Example("""
        if {_player} doesn't have a clipboard:
            send "You need a clipboard!" to {_player}
        """)
@RequiredPlugins("WorldEdit")
@Since("1.0")
public class CondHasClipboard extends PropertyCondition<Player> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
                SyntaxRegistry.CONDITION,
                infoBuilder(
                        CondHasClipboard.class,
                        PropertyType.HAVE,
                        "[a] clipboard[s]",
                        "players"
                )
                        .supplier(CondHasClipboard::new)
                        .build()
        );
    }

    @Override
    public boolean check(Player player) {
        LocalSession session = PlayerUtils.getSession(player);
        if (session == null) return false;
        try {
            return session.getClipboard() != null;
        } catch (EmptyClipboardException ignored) {
            return false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "clipboard";
    }
}
