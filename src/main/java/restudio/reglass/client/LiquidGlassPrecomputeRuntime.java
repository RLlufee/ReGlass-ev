package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.gui.QuadVertexBufferProvider;
import restudio.reglass.mixin.accessor.GameRendererAccessor;

public final class LiquidGlassPrecomputeRuntime {

    private static final LiquidGlassPrecomputeRuntime INSTANCE = new LiquidGlassPrecomputeRuntime();

    public static LiquidGlassPrecomputeRuntime get() {
        return INSTANCE;
    }

    private RenderPipeline blurPipeline;

    private GpuTexture blurTempTex;
    private GpuTextureView blurTempView;

    private final HashMap<Integer, GpuTexture> blurredByRadius = new HashMap<>();
    private final HashMap<Integer, GpuTextureView> blurredViewByRadius = new HashMap<>();

    private GpuBuffer samplerInfoUbo;
    private GpuBuffer blurConfigUboX;
    private GpuBuffer blurConfigUboY;

    private static final int MAX_RADIUS = 64;

    private List<Integer> requestedRadii = new ArrayList<>();

    private static final Identifier VS_ID = Identifier.fromNamespaceAndPath("reglass", "core/blit_fullscreen");
    private static final Identifier BLUR_ID = Identifier.fromNamespaceAndPath("reglass", "program/blur");

    private LiquidGlassPrecomputeRuntime() {}

    private void ensurePipelines() {
        if (blurPipeline == null) {
            blurPipeline = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("reglass", "pipeline/blur"))
                    .withVertexShader(VS_ID)
                    .withFragmentShader(BLUR_ID)
                    .withBindGroupLayout(
                            BindGroupLayout.builder()
                                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                                    .withUniform("Config", UniformType.UNIFORM_BUFFER)
                                    .withSampler("DiffuseSampler")
                                    .build()
                    )
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withColorTargetState(ColorTargetState.DEFAULT)
                    .build();
            RenderSystem.getDevice().precompilePipeline(blurPipeline);
        }

        if (samplerInfoUbo == null) {
            samplerInfoUbo = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo (pre)", 130, 16);
        }

        int blurConfigSize = 16 + (MAX_RADIUS + 1) * 16;
        if (blurConfigUboX == null) {
            blurConfigUboX = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig X", 130, blurConfigSize);
        }
        if (blurConfigUboY == null) {
            blurConfigUboY = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig Y", 130, blurConfigSize);
        }
    }

    private void ensureTempTarget(int w, int h) {
        if (blurTempTex == null || blurTempTex.getWidth(0) != w || blurTempTex.getHeight(0) != h) {
            if (blurTempTex != null) {
                if (blurTempView != null) blurTempView.close();
                blurTempTex.close();
            }
            blurTempTex = RenderSystem.getDevice().createTexture("reglass blurTemp", 12, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
            blurTempView = RenderSystem.getDevice().createTextureView(blurTempTex);
        }
    }

    private void ensureOutputForRadius(int w, int h, int radius) {
        GpuTexture tex = blurredByRadius.get(radius);
        if (tex == null || tex.getWidth(0) != w || tex.getHeight(0) != h) {
            if (tex != null) {
                GpuTextureView old = blurredViewByRadius.get(radius);
                if (old != null) old.close();
                tex.close();
            }
            GpuTexture newTex = RenderSystem.getDevice().createTexture("reglass blurred r=" + radius, 12, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
            GpuTextureView newView = RenderSystem.getDevice().createTextureView(newTex);
            blurredByRadius.put(radius, newTex);
            blurredViewByRadius.put(radius, newView);
        }
    }

    private static float[] gaussian(int radius) {
        radius = Math.max(0, Math.min(radius, MAX_RADIUS));
        float sigma = radius / 3.0f;
        if (radius == 0) return new float[] {1f};
        float[] kernel = new float[radius + 1];
        float sum = 0f;
        for (int i = 0; i <= radius; i++) {
            float w = (float) Math.exp(-0.5 * ((float) i * (float) i) / (sigma * sigma));
            kernel[i] = w;
            sum += (i == 0) ? w : (2f * w);
        }
        for (int i = 0; i <= radius; i++) kernel[i] /= sum;
        return kernel;
    }

    private void uploadBlur(GpuBuffer ubo, float dx, float dy, int radius) {
        radius = Math.max(0, Math.min(radius, MAX_RADIUS));
        float[] weights = gaussian(radius);
        try (var map = ubo.map(false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putVec4(dx, dy, (float) radius, 0f);
            for (int i = 0; i <= MAX_RADIUS; i++) {
                float w = (i <= radius) ? weights[i] : 0f;
                b.putFloat(w);
                b.align(16);
            }
        }
    }

    public void setRequestedRadii(List<Integer> ordered) {
        requestedRadii = new ArrayList<>(ordered);
    }

    public void run() {
        ensurePipelines();

        var mc = Minecraft.getInstance();
        var main = mc.gameRenderer.mainRenderTarget();
        int w = main.width;
        int h = main.height;

        ensureTempTarget(w, h);

        try (var map = samplerInfoUbo.map(false, true)) {
            Std140Builder.intoBuffer(map.data()).putVec2((float) w, (float) h).putVec2((float) w, (float) h);
        }

        var ce = RenderSystem.getDevice().createCommandEncoder();
        GameRenderer gameRenderer = mc.gameRenderer;
        GuiRenderer guiRenderer = ((GameRendererAccessor) gameRenderer).getGuiRenderer();
        var quadVB = ((QuadVertexBufferProvider) guiRenderer).getQuadVertexBuffer();
        var idxInfo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        var ib = idxInfo.getBuffer(6);
        var it = idxInfo.type();

        int max = Math.min(LiquidGlassUniforms.MAX_BLUR_LEVELS, requestedRadii == null ? 0 : requestedRadii.size());
        if (max == 0) {
            int r = Math.max(1, ReGlassConfig.INSTANCE.defaultBlurRadius);
            requestedRadii = List.of(r);
            max = 1;
        }

        for (int k = 0; k < max; k++) {
            int radius = Math.max(1, requestedRadii.get(k));

            ensureOutputForRadius(w, h, radius);

            uploadBlur(blurConfigUboX, 1f, 0f, radius);
            uploadBlur(blurConfigUboY, 0f, 1f, radius);

            try (RenderPass pass = ce.createRenderPass(() -> "reglass blur X r=" + radius, blurTempView, Optional.empty())) {
                pass.setPipeline(blurPipeline);
                pass.setUniform("SamplerInfo", samplerInfoUbo);
                pass.setUniform("Config", blurConfigUboX);
                pass.bindTexture("DiffuseSampler", main.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                pass.setVertexBuffer(0, quadVB.slice());
                pass.setIndexBuffer(ib, it);
                pass.drawIndexed(6, 1, 0, 0, 0);
            }

            try (RenderPass pass = ce.createRenderPass(() -> "reglass blur Y r=" + radius, blurredViewByRadius.get(radius), Optional.empty())) {
                pass.setPipeline(blurPipeline);
                pass.setUniform("SamplerInfo", samplerInfoUbo);
                pass.setUniform("Config", blurConfigUboY);
                pass.bindTexture("DiffuseSampler", blurTempView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                pass.setVertexBuffer(0, quadVB.slice());
                pass.setIndexBuffer(ib, it);
                pass.drawIndexed(6, 1, 0, 0, 0);
            }
        }
    }

    public GpuTextureView getBlurredViewForRadius(int radius) {
        return blurredViewByRadius.get(radius);
    }
}