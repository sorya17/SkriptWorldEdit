package me.tecspace.skriptworldedit.api.utils;

import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class TransformUtils {

    public static AffineTransform buildTransform(@Nullable Double rotationY, @Nullable Vector scale, @Nullable Vector offset) {
        AffineTransform transform = new AffineTransform();
        if (rotationY != null)
            transform = transform.rotateY(rotationY);
        if (scale != null)
            transform = transform.scale(scale.getX(), scale.getY(), scale.getZ());
        if (offset != null)
            transform = transform.translate(offset.getX(), offset.getY(), offset.getZ());
        return transform;
    }
}
