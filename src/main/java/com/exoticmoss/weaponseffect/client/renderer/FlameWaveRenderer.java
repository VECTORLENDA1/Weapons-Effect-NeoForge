package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.FlameWaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

/// Draws the Fire Sword ability: ribbons of fire and lava that twist around each other while they fly
/// from the sword to where the player is aiming. White-hot at the sword, then yellow, orange and dark
/// red at the end (blue soul fire at level V), and fire on the ground at level III and up.
/// Everything is 3D geometry (no particles). All the sizes come from FlameWaveEntity (the same numbers as the damage).
public class FlameWaveRenderer extends EntityRenderer<FlameWaveEntity, FlameWaveRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("weaponseffect", "textures/entity/flame_wave.png");

    // ---------------------------------------------------------------- LIGHT / BRIGHTNESS
    /// How bright the flames are: 1.0 = full colors, 0.5 = half as bright, 0 = black.
    private static final float BRIGHTNESS = 0.5F;
    /// true = the flames are "emissive": shaders make them glow (bloom/halo around them).
    /// false = no glow with shaders (they still have their colors and can be seen in the dark).
    private static final boolean EMISSIVE = true;
    /// Light of the flames: 15 = full light (seen bright even in the dark), 0 = dark like the place around.
    private static final int LIGHT_LEVEL = 15;

    private static final RenderType TYPE = EMISSIVE ? RenderTypes.entityTranslucentEmissive(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    private static final int FULL_BRIGHT = LIGHT_LEVEL << 4 | LIGHT_LEVEL << 20;

    /// How long (in blocks) each piece of a ribbon is. Smaller = smoother curves (but slower to draw).
    private static final float STEP = 0.2F;
    /// How many sides each ribbon has (it is a thin tube).
    private static final int SIDES = 6;
    /// Thickness (radius, in blocks) of the ribbons at the sword and at the end.
    private static final float START_THICKNESS = 0.07F;
    private static final float END_THICKNESS = 0.3F;
    /// How many turns the ribbons do around each other per block, and how fast they spin.
    private static final float TWIST = 0.9F;
    private static final float SPIN = 0.35F;
    /// The flames are wider than tall: height = WIDTH x FLATNESS (1 = round, smaller = flatter).
    private static final float FLATNESS = 0.6F;
    /// Colors along the flames (0xAARRGGBB), from the sword (first) to the end (last).
    private static final int[] COLORS = {0xFFFFFFD0, 0xFFFFE070, 0xFFFFA020, 0xF0F05000, 0xC0B01800, 0x60600800};
    /// The soft glow around all the ribbons (a bigger transparent tube in the middle).
    private static final int GLOW_COLOR = 0x50FF7010;
    /// The same colors for the blue soul fire (level V).
    private static final int[] SOUL_COLORS = {0xFFF0FFFF, 0xFFA0F8FF, 0xFF40D8F0, 0xF02090D0, 0xC0105090, 0x60082850};
    private static final int SOUL_GLOW_COLOR = 0x5030C0F0;
    /// Fire on the ground (level III and up): height of the flames and their colors (bottom, top).
    private static final float GROUND_FLAME_HEIGHT = 0.9F;
    private static final int GROUND_BOTTOM = 0xE0FFB030, GROUND_TOP = 0x00FF3000;
    private static final int SOUL_GROUND_BOTTOM = 0xE060E8FF, SOUL_GROUND_TOP = 0x002060F0;

    public static class State extends EntityRenderState {
        float head;
        float tail;
        float time;
        float reach;
        float endWidth;
        boolean soul;
        float groundStrength;
        final List<Vector3f> ground = new ArrayList<>();

        /// Half of the width of the flames at this distance from the sword.
        float halfWidthAt(float distance) {
            return Mth.lerp(Mth.clamp(distance / reach, 0, 1), FlameWaveEntity.START_WIDTH, endWidth) / 2;
        }
        final Vector3f direction = new Vector3f();
        final Vector3f side = new Vector3f();
        final Vector3f up = new Vector3f();
    }

    public FlameWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FlameWaveEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.head = entity.getHead(partialTicks);
        state.tail = entity.getTail(partialTicks);
        state.time = entity.tickCount + partialTicks;
        state.reach = entity.reach();
        state.endWidth = entity.halfWidthAt(state.reach) * 2;
        state.soul = entity.isSoulFire();
        state.groundStrength = entity.getGroundFireStrength(partialTicks);
        state.ground.clear();
        for (Vec3 fire : entity.getGroundFire()) {
            Vec3 relative = fire.subtract(entity.position());
            if (relative.length() <= state.head + 1) state.ground.add(new Vector3f((float) relative.x, (float) relative.y, (float) relative.z));
        }

        // Direction of the flames, and two directions across them (to the side and "up")
        Vec3 d = entity.getFlameDirection();
        Vec3 s = d.cross(new Vec3(0, 1, 0));
        if (s.lengthSqr() < 1.0E-4) s = new Vec3(1, 0, 0); // aiming straight up or down
        s = s.normalize();
        Vec3 u = s.cross(d).normalize();
        state.direction.set((float) d.x, (float) d.y, (float) d.z);
        state.side.set((float) s.x, (float) s.y, (float) s.z);
        state.up.set((float) u.x, (float) u.y, (float) u.z);
    }

    @Override
    protected AABB getBoundingBoxForCulling(FlameWaveEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(entity.reach() + entity.halfWidthAt(entity.reach()) + 2);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.head - state.tail > 0.05F) {
            collector.submitCustomGeometry(poseStack, TYPE, (pose, v) -> {
                for (int r = 0; r < FlameWaveEntity.RIBBONS; r++) drawRibbon(pose, v, state, r);
                drawGlow(pose, v, state);
            });
        }
        if (!state.ground.isEmpty() && state.groundStrength > 0) {
            collector.submitCustomGeometry(poseStack, TYPE, (pose, v) -> {
                for (int i = 0; i < state.ground.size(); i++) drawGroundFire(pose, v, state, state.ground.get(i), i);
            });
        }
        super.submit(state, poseStack, collector, camera);
    }

    /// One ribbon: a thin tube that turns around the middle line of the flames.
    private static void drawRibbon(PoseStack.Pose pose, VertexConsumer v, State s, int ribbon) {
        float offset = Mth.TWO_PI * ribbon / FlameWaveEntity.RIBBONS;
        Vector3f[] previous = null;
        int[] previousColors = null;

        for (float along = s.tail; ; along += STEP) {
            boolean last = along >= s.head;
            if (last) along = s.head;

            // Where the ribbon is: turns around the middle line, and opens up further away
            float angle = offset + along * TWIST * Mth.TWO_PI / 2 - s.time * SPIN;
            // The outer edge of the ribbons never goes past the WIDTH of the flames
            float thickness = thicknessAt(s, along);
            float wiggle = 0.9F + 0.1F * Mth.sin(along * 1.7F + s.time * 0.3F + ribbon);
            float radius = (s.halfWidthAt(along) - thickness) * wiggle;
            Vector3f center = new Vector3f(s.direction).mul(along)
                    .add(new Vector3f(s.side).mul(Mth.cos(angle) * radius))
                    .add(new Vector3f(s.up).mul(Mth.sin(angle) * radius * FLATNESS));

            thickness *= ends(s, along);
            int color = colorAt(s, along, ribbon);

            Vector3f[] ring = new Vector3f[SIDES];
            int[] colors = new int[SIDES];
            for (int i = 0; i < SIDES; i++) {
                float a = Mth.TWO_PI * i / SIDES;
                ring[i] = new Vector3f(center)
                        .add(new Vector3f(s.side).mul(Mth.cos(a) * thickness))
                        .add(new Vector3f(s.up).mul(Mth.sin(a) * thickness));
                colors[i] = color;
            }
            if (previous != null) tube(pose, v, previous, ring, previousColors, colors);
            previous = ring;
            previousColors = colors;
            if (last) break;
        }
    }

    /// A big soft transparent tube in the middle of the ribbons, so the flames look full and glowing.
    private static void drawGlow(PoseStack.Pose pose, VertexConsumer v, State s) {
        Vector3f[] previous = null;
        int[] previousColors = null;
        for (float along = s.tail; ; along += STEP * 2) {
            boolean last = along >= s.head;
            if (last) along = s.head;

            Vector3f center = new Vector3f(s.direction).mul(along);
            float radius = (s.halfWidthAt(along) - thicknessAt(s, along)) * 0.8F * ends(s, along);
            int glow = s.soul ? SOUL_GLOW_COLOR : GLOW_COLOR;
            int alpha = Math.round(ARGB.alpha(glow) * (1 - along / s.reach * 0.6F));
            Vector3f[] ring = new Vector3f[SIDES * 2];
            int[] colors = new int[SIDES * 2];
            for (int i = 0; i < ring.length; i++) {
                float a = Mth.TWO_PI * i / ring.length;
                ring[i] = new Vector3f(center)
                        .add(new Vector3f(s.side).mul(Mth.cos(a) * radius))
                        .add(new Vector3f(s.up).mul(Mth.sin(a) * radius * FLATNESS));
                colors[i] = ARGB.color(alpha, ARGB.scaleRGB(glow, BRIGHTNESS));
            }
            if (previous != null) tube(pose, v, previous, ring, previousColors, colors);
            previous = ring;
            previousColors = colors;
            if (last) break;
        }
    }

    /// Thickness of a ribbon at this distance (never more than half of the flames' half width).
    private static float thicknessAt(State s, float along) {
        float thickness = Mth.lerp(Mth.clamp(along / s.reach, 0, 1), START_THICKNESS, END_THICKNESS);
        return Math.min(thickness, s.halfWidthAt(along) * 0.5F);
    }

    /// The flames get thin at the front and at the end, so they don't look cut.
    private static float ends(State s, float along) {
        float fromTail = s.tail > 0 ? (along - s.tail) / 1.2F : 1;
        float fromHead = (s.head - along) / 0.8F;
        return Mth.clamp(Math.min(fromTail, fromHead), 0.15F, 1);
    }

    /// Color at this distance from the sword, with brighter pulses running along the flames.
    private static int colorAt(State s, float along, int ribbon) {
        int[] colors = s.soul ? SOUL_COLORS : COLORS;
        float t = Mth.clamp(along / s.reach, 0, 1) * (colors.length - 1);
        int index = Math.min((int) t, colors.length - 2);
        int color = ARGB.srgbLerp(t - index, colors[index], colors[index + 1]);
        float pulse = 0.78F + 0.22F * Mth.sin(along * 3 - s.time * 1.2F + ribbon);
        return ARGB.color(ARGB.alpha(color), ARGB.scaleRGB(color, pulse * BRIGHTNESS));
    }

    /// Fire on the ground: three flat flames crossing each other, moving up and down.
    private static void drawGroundFire(PoseStack.Pose pose, VertexConsumer v, State s, Vector3f at, int index) {
        int bottom = ARGB.color(Math.round(ARGB.alpha(s.soul ? SOUL_GROUND_BOTTOM : GROUND_BOTTOM) * s.groundStrength),
                ARGB.scaleRGB(s.soul ? SOUL_GROUND_BOTTOM : GROUND_BOTTOM, BRIGHTNESS));
        int top = s.soul ? SOUL_GROUND_TOP : GROUND_TOP;
        for (int i = 0; i < 3; i++) {
            float angle = Mth.PI / 3 * i + index * 0.7F;
            float dx = Mth.cos(angle) * 0.45F, dz = Mth.sin(angle) * 0.45F;
            float height = GROUND_FLAME_HEIGHT * s.groundStrength * (0.7F + 0.3F * Mth.sin(s.time * 0.6F + index * 1.3F + i));
            Vector3f a = new Vector3f(at.x - dx, at.y, at.z - dz);
            Vector3f b = new Vector3f(at.x + dx, at.y, at.z + dz);
            Vector3f tip = new Vector3f(at.x, at.y + height, at.z);
            // a pointed flame (drawn as a quad with the tip twice), both sides
            vertex(pose, v, a, bottom);
            vertex(pose, v, b, bottom);
            vertex(pose, v, tip, top);
            vertex(pose, v, tip, top);
            vertex(pose, v, tip, top);
            vertex(pose, v, tip, top);
            vertex(pose, v, b, bottom);
            vertex(pose, v, a, bottom);
        }
    }

    /// Joins two rings of points with quads (both sides, so it looks right from inside too).
    private static void tube(PoseStack.Pose pose, VertexConsumer v, Vector3f[] a, Vector3f[] b, int[] ca, int[] cb) {
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            vertex(pose, v, a[i], ca[i]);
            vertex(pose, v, a[j], ca[j]);
            vertex(pose, v, b[j], cb[j]);
            vertex(pose, v, b[i], cb[i]);

            vertex(pose, v, b[i], cb[i]);
            vertex(pose, v, b[j], cb[j]);
            vertex(pose, v, a[j], ca[j]);
            vertex(pose, v, a[i], ca[i]);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer v, Vector3f p, int color) {
        v.addVertex(pose, p.x, p.y, p.z)
                .setColor(color)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, 0, 1, 0);
    }
}
