package me.tecspace.skriptworldedit.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sk89q.worldedit.function.mask.Mask;
import org.skriptlang.skript.addon.SkriptAddon;

public class MaskClassInfo {

    public static void register(SkriptAddon addon) {
        Classes.registerClass(new ClassInfo<>(Mask.class, "worldeditmask")
                .user("worldedit ?masks?")
                .name("WorldEdit Mask")
                .description("A WorldEdit Mask")
                .requiredPlugins("WorldEdit")
                .since("1.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(Mask mask, int flags) {
                        return mask.toString();
                    }

                    @Override
                    public String toVariableNameString(Mask mask) {
                        return mask.toString();
                    }
                })
        );
    }
}