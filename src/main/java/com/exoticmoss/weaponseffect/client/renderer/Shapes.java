package com.exoticmoss.weaponseffect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/// Small helpers to draw the 3D abilities (ice shards, poison cloud, hammer shockwave...).
/// Everything uses a plain white texture, the colors come from each renderer.
public final class Shapes {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("weaponseffect", "textures/entity/white.png");
    /// See-through and glowing (shaders make it shine).
    public static final RenderType GLOWING = RenderTypes.entityTranslucentEmissive(WHITE);
    /// See-through, without glow.
    public static final RenderType TRANSLUCENT = RenderTypes.entityTranslucent(WHITE);
    /// Full light: seen even in the dark.
    public static final int FULL_BRIGHT = 0xF000F0;

    private Shapes() {
    }

    /// One corner of a shape. Color is 0xAARRGGBB.
    public static void vertex(PoseStack.Pose pose, VertexConsumer v, float x, float y, float z, int color, int light, float nx, float ny, float nz) {
        v.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    /// A triangle seen from both sides (drawn as quads with one corner repeated).
    public static void triangle(PoseStack.Pose pose, VertexConsumer v, Vector3f a, Vector3f b, Vector3f c, int color, int light) {
        Vector3f n = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
        vertex(pose, v, a.x, a.y, a.z, color, light, n.x, n.y, n.z);
        vertex(pose, v, b.x, b.y, b.z, color, light, n.x, n.y, n.z);
        vertex(pose, v, c.x, c.y, c.z, color, light, n.x, n.y, n.z);
        vertex(pose, v, c.x, c.y, c.z, color, light, n.x, n.y, n.z);
        vertex(pose, v, c.x, c.y, c.z, color, light, -n.x, -n.y, -n.z);
        vertex(pose, v, b.x, b.y, b.z, color, light, -n.x, -n.y, -n.z);
        vertex(pose, v, a.x, a.y, a.z, color, light, -n.x, -n.y, -n.z);
        vertex(pose, v, a.x, a.y, a.z, color, light, -n.x, -n.y, -n.z);
    }

    /// A ball of the given radius around the center of the pose.
    /// "wobble" (0 = perfect ball) makes it lumpy and moving, like smoke, using the time.
    public static void sphere(PoseStack.Pose pose, VertexConsumer v, float radius, int color, int light, float wobble, float time) {
        int detail = 16;
        for (int lat = 0; lat < detail; lat++) {
            for (int lon = 0; lon < detail; lon++) {
                spherePoint(pose, v, lat, lon, detail, radius, color, light, wobble, time);
                spherePoint(pose, v, lat + 1, lon, detail, radius, color, light, wobble, time);
                spherePoint(pose, v, lat + 1, lon + 1, detail, radius, color, light, wobble, time);
                spherePoint(pose, v, lat, lon + 1, detail, radius, color, light, wobble, time);
            }
        }
    }

    private static void spherePoint(PoseStack.Pose pose, VertexConsumer v, int lat, int lon, int detail, float radius,
                                    int color, int light, float wobble, float time) {
        float theta = Mth.PI * lat / detail;
        float phi = Mth.TWO_PI * lon / detail;
        float x = Mth.sin(theta) * Mth.cos(phi), y = Mth.cos(theta), z = Mth.sin(theta) * Mth.sin(phi);
        float r = radius * (1 + wobble * Mth.sin(x * 3 + time * 0.15F) * Mth.cos(z * 3 - time * 0.1F + y * 2));
        vertex(pose, v, x * r, y * r, z * r, color, light, x, y, z);
    }

    /// A flat ring on the ground (seen from above and below), from the inner to the outer radius.
    public static void ring(PoseStack.Pose pose, VertexConsumer v, float inner, float outer, int innerColor, int outerColor, int light) {
        int pieces = 48;
        for (int i = 0; i < pieces; i++) {
            float a1 = Mth.TWO_PI * i / pieces, a2 = Mth.TWO_PI * (i + 1) / pieces;
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1), c2 = Mth.cos(a2), s2 = Mth.sin(a2);
            vertex(pose, v, c1 * inner, 0, s1 * inner, innerColor, light, 0, 1, 0);
            vertex(pose, v, c1 * outer, 0, s1 * outer, outerColor, light, 0, 1, 0);
            vertex(pose, v, c2 * outer, 0, s2 * outer, outerColor, light, 0, 1, 0);
            vertex(pose, v, c2 * inner, 0, s2 * inner, innerColor, light, 0, 1, 0);
            vertex(pose, v, c2 * inner, 0, s2 * inner, innerColor, light, 0, -1, 0);
            vertex(pose, v, c2 * outer, 0, s2 * outer, outerColor, light, 0, -1, 0);
            vertex(pose, v, c1 * outer, 0, s1 * outer, outerColor, light, 0, -1, 0);
            vertex(pose, v, c1 * inner, 0, s1 * inner, innerColor, light, 0, -1, 0);
        }
    }
}
