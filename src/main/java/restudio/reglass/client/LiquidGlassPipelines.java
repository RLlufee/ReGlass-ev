package restudio.reglass.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;

public final class LiquidGlassPipelines {
    private static RenderPipeline LIQUID_GLASS_GUI;

    private LiquidGlassPipelines() {}

    public static synchronized RenderPipeline getGuiPipeline() {
        if (LIQUID_GLASS_GUI == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("reglass", "program/liquid_glass_gui"))
                    //Vulkan requires pipeline layouts to match actual shader bindings
                    //unused entries can break pipeline creation
                    .withBindGroupLayout(
                            BindGroupLayout.builder()
                                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                                    .withUniform("CustomUniforms", UniformType.UNIFORM_BUFFER)
                                    .withUniform("WidgetInfo", UniformType.UNIFORM_BUFFER)
                                    .withUniform("BgConfig", UniformType.UNIFORM_BUFFER)
                                    .withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .withUniform("Sampler1", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .withUniform("Sampler2", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .withUniform("Sampler3", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .withUniform("Sampler4", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .withUniform("Sampler5", UniformType.COMBINED_IMAGE_SAMPLER)
                                    .build()
                    )
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    //Minecraft leaves the no-depth Vulkan pipeline variant invalid, which can crash when bound.
                    //the blend attachment still has to match the color target format
                    .withColorTargetState(ColorTargetState.DEFAULT);

            LIQUID_GLASS_GUI = b.build();
        }
        return LIQUID_GLASS_GUI;
    }
}