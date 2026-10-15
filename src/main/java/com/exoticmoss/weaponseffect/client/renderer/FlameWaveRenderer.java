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
import org.joml.Vector3f;

/// Draws the Fire Sword ability: ribbons of fire and lava that twist around each other while they fly
/// from the sword to where the player is aiming. White-hot at the sword, then yellow, orange and dark
/// red at the end. Everything is 3D geometry (no particles).
public class FlameWaveRenderer extends EntityRenderer<FlameWaveEntity, FlameWaveRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("weaponseffect", "textures/entity/flame_wave.png");

    // ---------------------------------------------------------------- LIGHT / BRIGHTNESS
    /// How bright the flames are: 1.0 = full colors, 0.5 = half as bright, 0 = black.
    private static final float BRIGHTNESS = 0.75F;
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

    public static class State extends EntityRenderState {
        float head;
        float tail;
        float time;
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
        return super.getBoundingBoxForCulling(entity).inflate(FlameWaveEntity.LENGTH + FlameWaveEntity.WIDTH + 1);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.head - state.tail > 0.05F) {
            collector.submitCustomGeometry(poseStack, TYPE, (pose, v) -> {
                for (int r = 0; r < FlameWaveEntity.RIBBONS; r++) drawRibbon(pose, v, state, r);
                drawGlow(pose, v, state);
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
            float thickness = thicknessAt(along);
            float wiggle = 0.9F + 0.1F * Mth.sin(along * 1.7F + s.time * 0.3F + ribbon);
            float radius = (FlameWaveEntity.halfWidthAt(along) - thickness) * wiggle;
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
            float radius = (FlameWaveEntity.halfWidthAt(along) - thicknessAt(along)) * 0.8F * ends(s, along);
            int alpha = Math.round(ARGB.alpha(GLOW_COLOR) * (1 - along / FlameWaveEntity.reach() * 0.6F));
            Vector3f[] ring = new Vector3f[SIDES * 2];
            int[] colors = new int[SIDES * 2];
            for (int i = 0; i < ring.length; i++) {
                float a = Mth.TWO_PI * i / ring.length;
                ring[i] = new Vector3f(center)
                        .add(new Vector3f(s.side).mul(Mth.cos(a) * radius))
                        .add(new Vector3f(s.up).mul(Mth.sin(a) * radius * FLATNESS));
                colors[i] = ARGB.color(alpha, ARGB.scaleRGB(GLOW_COLOR, BRIGHTNESS));
            }
            if (previous != null) tube(pose, v, previous, ring, previousColors, colors);
            previous = ring;
            previousColors = colors;
            if (last) break;
        }
    }

    /// Thickness of a ribbon at this distance (never more than half of the flames' half width).
    private static float thicknessAt(float along) {
        float thickness = Mth.lerp(Mth.clamp(along / FlameWaveEntity.reach(), 0, 1), START_THICKNESS, END_THICKNESS);
        return Math.min(thickness, FlameWaveEntity.halfWidthAt(along) * 0.5F);
    }

    /// The flames get thin at the front and at the end, so they don't look cut.
    private static float ends(State s, float along) {
        float fromTail = s.tail > 0 ? (along - s.tail) / 1.2F : 1;
        float fromHead = (s.head - along) / 0.8F;
        return Mth.clamp(Math.min(fromTail, fromHead), 0.15F, 1);
    }

    /// Color at this distance from the sword, with brighter pulses running along the flames.
    private static int colorAt(State s, float along, int ribbon) {
        float t = Mth.clamp(along / FlameWaveEntity.reach(), 0, 1) * (COLORS.length - 1);
        int index = Math.min((int) t, COLORS.length - 2);
        int color = ARGB.srgbLerp(t - index, COLORS[index], COLORS[index + 1]);
        float pulse = 0.78F + 0.22F * Mth.sin(along * 3 - s.time * 1.2F + ribbon);
        return ARGB.color(ARGB.alpha(color), ARGB.scaleRGB(color, pulse * BRIGHTNESS));
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
