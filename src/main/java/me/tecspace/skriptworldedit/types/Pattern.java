package me.tecspace.skriptworldedit.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import me.tecspace.skriptworldedit.api.PatternWrapper;
import org.skriptlang.skript.addon.SkriptAddon;

public class Pattern {

    public static void register(SkriptAddon skriptAddon) {
        Classes.registerClass(new ClassInfo<>(PatternWrapper.class, "worldeditpattern")
                .user("worldedit ?patterns?")
                .name("WorldEdit Pattern")
                .description("A WorldEdit Pattern")
                .requiredPlugins("WorldEdit")
                .since("1.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(PatternWrapper pattern, int flags) {
                        return pattern.describe();
                    }

                    @Override
                    public String toVariableNameString(PatternWrapper wrapper) {
                        return wrapper.toString();
                    }
                })
        );
    }
}