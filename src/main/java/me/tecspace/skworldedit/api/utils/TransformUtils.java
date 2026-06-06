package me.tecspace.skworldedit.api.utils;

import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class TransformUtils {

    public static AffineTransform VERTICAL_FLIP = new AffineTransform().scale(1, -1, 1);

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

    public static AffineTransform buildTransform(
            @Nullable Quaternionf rotation,
            @Nullable Vector scale,
            @Nullable Vector offset) {

        AffineTransform transform;
        if (rotation != null) {
            transform = transformOfQuaternion(rotation);
        } else {
            transform = new AffineTransform();
        }

        if (scale != null)
            transform = transform.scale(scale.getX(), scale.getY(), scale.getZ());

        if (offset != null)
            transform = transform.translate(offset.getX(), offset.getY(), offset.getZ());

        return transform;
    }

    public static AffineTransform transformOfQuaternion(Quaternionf quaternion) {
        Matrix4f mat = quaternion.get(new Matrix4f());
        return new AffineTransform(
                mat.m00(), mat.m01(), mat.m02(), 0,
                mat.m10(), mat.m11(), mat.m12(), 0,
                mat.m20(), mat.m21(), mat.m22(), 0
        );
    }
}
