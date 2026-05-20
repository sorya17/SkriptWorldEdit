package me.tecspace.skriptworldedit.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sk89q.worldedit.function.pattern.Pattern;
import org.skriptlang.skript.addon.SkriptAddon;

public class PatternClassInfo {

    public static void register(SkriptAddon addon) {
        Classes.registerClass(new ClassInfo<>(Pattern.class, "worldeditpattern")
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
                    public String toString(Pattern pattern, int flags) {
                        return pattern.toString();
                    }

                    @Override
                    public String toVariableNameString(Pattern pattern) {
                        return pattern.toString();
                    }
                })
        );
    }
}