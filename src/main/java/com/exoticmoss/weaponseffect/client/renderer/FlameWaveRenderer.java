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

/// Draws the Fire Sword ability as a TSUNAMI OF LAVA: a wave that rises behind the player,
/// curls its crest forward (white-hot at the top), runs forward getting wider and crashes at the end.
/// Everything is 3D geometry (no particles), with glowing bands of lava flowing over it.
public class FlameWaveRenderer extends EntityRenderer<FlameWaveEntity, FlameWaveRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityTranslucentEmissive(
            Identifier.fromNamespaceAndPath("weaponseffect", "textures/entity/flame_wave.png"));
    private static final int FULL_BRIGHT = 0xF000F0;
    /// How many pieces the wave has across its width. More = smoother (but slower to draw).
    private static final int COLUMNS = 28;

    /// The shape of the wave seen from the side: {forward, height, color}.
    /// forward is in blocks from the front of the wave (negative = behind), height goes from 0 (ground)
    /// to 1 (top of the crest, multiplied by HEIGHT). Change these points to change the shape of the tsunami.
    private static final float[][] PROFILE = {
            {-5.0f, 0.00f}, // lava trail on the ground, fading out
            {-3.2f, 0.04f},
            {-2.2f, 0.18f}, // the back of the wave rising
            {-1.4f, 0.45f},
            {-0.7f, 0.78f},
            {-0.15f, 1.00f}, // crest
            {0.35f, 0.97f}, // the crest curling forward
            {0.70f, 0.80f}, // tip of the lip
            {0.55f, 0.62f}, // under the lip (curling back)
            {0.22f, 0.52f},
            {0.05f, 0.32f}, // front face going down
            {0.25f, 0.00f}, // foot of the wave
    };
    /// Color of each point of PROFILE (0xAARRGGBB): dark red lava at the back, white-hot at the crest.
    private static final int[] COLORS = {
            0x00400800,
            0x90701000,
            0xD0A01C00,
            0xE8D03A00,
            0xF5FF6A00,
            0xFFFFB030,
            0xFFFFE890,
            0xFFFFFFD8,
            0xF5FFA040,
            0xEEFF7A10,
            0xE8F05000,
            0xD0B01800,
    };
    /// Bands of brighter lava flowing over the wave: how many and how fast.
    private static final float BANDS = 10;
    private static final float BAND_SPEED = 0.45f;

    public static class State extends EntityRenderState {
        float distance;
        float width;
        float progress;
        float time;
        float forwardX, forwardZ;
        /// Height of the ground under each column, compared to the entity
        final float[] ground = new float[COLUMNS + 1];
        /// false = this column is inside a wall (not drawn)
        final boolean[] visible = new boolean[COLUMNS + 1];
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
        state.distance = entity.getDistance(partialTicks);
        state.width = entity.getWidth(partialTicks);
        state.progress = entity.getProgress(partialTicks);
        state.time = entity.tickCount + partialTicks;
        Vec3 forward = entity.getForward();
        state.forwardX = (float) forward.x;
        state.forwardZ = (float) forward.z;

        // The wave follows the ground (goes up and down steps)
        float half = state.width / 2;
        for (int i = 0; i <= COLUMNS; i++) {
            float side = -1 + 2f * i / COLUMNS;
            float ahead = state.distance - FlameWaveEntity.CURVE * side * side;
            double x = entity.getX() + forward.x * ahead - forward.z * side * half;
            double z = entity.getZ() + forward.z * ahead + forward.x * side * half;
            double ground = entity.findGround(x, z);
            state.visible[i] = !Double.isNaN(ground);
            state.ground[i] = state.visible[i] ? (float) (ground - entity.getY()) : 0;
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(FlameWaveEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(FlameWaveEntity.LENGTH + 6, FlameWaveEntity.HEIGHT + 3, FlameWaveEntity.LENGTH + 6);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitCustomGeometry(poseStack, TYPE, (pose, v) -> drawTsunami(pose, v, state));
        super.submit(state, poseStack, collector, camera);
    }

    private static void drawTsunami(PoseStack.Pose pose, VertexConsumer v, State s) {
        float p = s.progress;
        // Rises in the beginning, stays big, crashes (gets low) at the end
        float rise = p < 0.25f ? Mth.lerp(p / 0.25f, 0.35f, 1f) : p > 0.75f ? Mth.lerp((p - 0.75f) / 0.25f, 1f, 0.25f) : 1f;
        // Appears quickly and fades out at the end
        float alpha = Math.min(1, s.time / 3f) * (p > 0.8f ? (1 - p) / 0.2f : 1);
        float half = s.width / 2;

        for (int i = 0; i < COLUMNS; i++) {
            if (!s.visible[i] || !s.visible[i + 1]) continue;
            for (int k = 0; k < PROFILE.length - 1; k++) {
                // 4 corners: column i/i+1, profile point k/k+1
                float[] a = point(s, i, k, half, rise);
                float[] b = point(s, i + 1, k, half, rise);
                float[] c = point(s, i + 1, k + 1, half, rise);
                float[] d = point(s, i, k + 1, half, rise);
                int ca = color(s, i, k, alpha), cb = color(s, i + 1, k, alpha);
                int cc = color(s, i + 1, k + 1, alpha), cd = color(s, i, k + 1, alpha);
                // both sides (seen from the front and from behind)
                quad(pose, v, a, b, c, d, ca, cb, cc, cd);
                quad(pose, v, d, c, b, a, cd, cc, cb, ca);
            }
        }
    }

    /// Position of one point of the wave: column (across the width) and profile point (along the side shape).
    private static float[] point(State s, int column, int k, float half, float rise) {
        float side = -1 + 2f * column / COLUMNS; // -1 left edge, 1 right edge
        // The wave is lower at its two ends
        float edge = 0.25f + 0.75f * Mth.sqrt(Math.max(0, 1 - side * side));
        // The crest moves a bit, like a real wave
        float wobble = 1 + 0.08f * Mth.sin(side * 6 + s.time * 0.4f);
        float height = PROFILE[k][1] * FlameWaveEntity.HEIGHT * rise * edge * wobble;
        // The curl of the crest is also smaller where the wave is lower
        float forward = s.distance - FlameWaveEntity.CURVE * side * side + PROFILE[k][0] * (0.5f + 0.5f * edge * rise);
        float lateral = side * half;

        float x = s.forwardX * forward - s.forwardZ * lateral;
        float z = s.forwardZ * forward + s.forwardX * lateral;
        return new float[]{x, s.ground[column] + height + 0.02f, z};
    }

    /// Color of one point, with bands of brighter lava flowing from the back to the crest.
    private static int color(State s, int column, int k, float alpha) {
        float side = -1 + 2f * column / COLUMNS;
        float along = (float) k / (PROFILE.length - 1);
        float band = 0.5f + 0.5f * Mth.sin(along * BANDS - s.time * BAND_SPEED + side * 2.5f);
        int color = ARGB.scaleRGB(COLORS[k], 0.72f + 0.28f * band);
        return ARGB.color(Math.round(ARGB.alpha(COLORS[k]) * alpha), color);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer v, float[] a, float[] b, float[] c, float[] d,
                             int ca, int cb, int cc, int cd) {
        // Normal of the quad (only used for the shading of shaders)
        float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        float wx = d[0] - a[0], wy = d[1] - a[1], wz = d[2] - a[2];
        float nx = uy * wz - uz * wy, ny = uz * wx - ux * wz, nz = ux * wy - uy * wx;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-5f) {
            nx = 0; ny = 1; nz = 0;
        } else {
            nx /= len; ny /= len; nz /= len;
        }
        vertex(pose, v, a, ca, nx, ny, nz);
        vertex(pose, v, b, cb, nx, ny, nz);
        vertex(pose, v, c, cc, nx, ny, nz);
        vertex(pose, v, d, cd, nx, ny, nz);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer v, float[] p, int color, float nx, float ny, float nz) {
        v.addVertex(pose, p[0], p[1], p[2])
                .setColor(color)
                .setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
