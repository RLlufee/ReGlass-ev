package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassPipelines;
import restudio.reglass.client.LiquidGlassPrecomputeRuntime;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.gui.QuadVertexBufferProvider;
import restudio.reglass.client.runtime.ReGlassAnim;
import restudio.reglass.mixin.accessor.GameRendererAccessor;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extract", at = @At("HEAD"))
    private void reglass(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        double deltaTicks;
        try {
            deltaTicks = tickCounter.getRealtimeDeltaTicks();
        } catch (Throwable t) {
            deltaTicks = 1.0 / 60.0 * 20.0;
        }
        double dt = deltaTicks / 20.0;
        LiquidGlassUniforms.get().beginFrame(dt);
        ReGlassAnim.INSTANCE.update(ReGlassConfig.INSTANCE, dt);
    }

    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
    private void reglass(CallbackInfo ci) {
        LiquidGlassUniforms uniforms = LiquidGlassUniforms.get();
        if (uniforms.getCount() > 0) {
            ci.cancel();
            uniforms.uploadSharedUniforms();
            uniforms.uploadWidgetInfo();
            List<Integer> radii = uniforms.getUsedBlurRadiiOrdered();
            LiquidGlassPrecomputeRuntime.get().setRequestedRadii(radii);
            LiquidGlassPrecomputeRuntime.get().run();
            RenderTarget mainFb = this.minecraft.gameRenderer.mainRenderTarget();
            CommandEncoder ce = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = ce.createRenderPass(
                    () -> "reglass liquid glass pass",
                    mainFb.getColorTextureView(),
                    Optional.empty(),
                    mainFb.hasDepth() ? mainFb.getDepthTextureView() : null,
                    OptionalDouble.empty()
            )) {
                RenderPipeline pipeline = LiquidGlassPipelines.getGuiPipeline();
                pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));

                pass.setUniform("SamplerInfo", uniforms.getSamplerInfoBuffer());
                pass.setUniform("CustomUniforms", uniforms.getCustomUniformsBuffer());
                pass.setUniform("WidgetInfo", uniforms.getWidgetInfoBuffer());
                pass.setUniform("BgConfig", uniforms.getBgConfigBuffer());
                pass.setUniform("Sampler0", mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

                GuiRenderer guiRenderer = ((GameRendererAccessor) this).getGuiRenderer();
                GpuBuffer quadVB = ((QuadVertexBufferProvider) guiRenderer).getQuadVertexBuffer();
                RenderSystem.AutoStorageIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
                GpuBuffer quadIB = quadIBInfo.getBuffer(6);
                pass.setVertexBuffer(0, quadVB.slice());
                pass.setIndexBuffer(quadIB, quadIBInfo.type());
                for (int i = 0; i < 5; i++) {
                    String samplerName = switch (i) {
                        case 0 -> "Sampler1";
                        case 1 -> "Sampler2";
                        case 2 -> "Sampler3";
                        case 3 -> "Sampler4";
                        default -> "Sampler5";
                    };
                    if (i < radii.size()) {
                        int r = radii.get(i);
                        if (r <= 0) pass.setUniform(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                        else pass.setUniform(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                    } else {
                        if (!radii.isEmpty()) {
                            int r0 = radii.getFirst();
                            if (r0 <= 0) pass.setUniform(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                            else pass.setUniform(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r0), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                        } else pass.setUniform(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                    }
                }
                pass.drawIndexed(6, 1, 0, 0, 0);
            }
            ce.submit();
        }
    }
}