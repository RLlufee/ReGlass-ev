## 本版本（26.3-ev）移植与修改日志：
- **着色器 SPIR-V 语法规范化修复（解决客户端崩溃）**：
  - 修复 Minecraft 26.3 RenderPearl 渲染引擎在将 GLSL 编译为 SPIR-V 字节码时的编译报错（'location' : SPIR-V requires location for user input/output）。
  - 为 lit_fullscreen.vsh、lur.fsh、liquid_glass_gui.fsh、g.fsh、loom.fsh 全部补齐 #version 330、#extension GL_ARB_separate_shader_objects : require 以及 layout(location = 0) 输入/输出变量显式修饰符，确保在 Vulkan/OpenGL 下顺利通过 SPIR-V 语法校验与管线构建。
- **完整移植至 Minecraft 26.3 (Fabric)**：
  - **渲染后端 RenderPearl 全新架构适配**：
    - 适配 Mojang 26.3 全新 com.mojang.renderpearl 抽象层，将所有管线、着色器描述符、缓冲区与渲染通路迁移至 RenderPearl API。
    - 适配 RenderPipeline, PrimitiveTopology, UniformType, BindGroupLayout, ColorTargetState。
    - 将 RenderPass 的纹理与缓冲绑定统一迁移为 setUniform，适配 CompiledRenderPipeline 编译调度。
    - 适配 GpuBuffer、GpuBufferSlice 与 AutoStorageIndexBuffer，解决缓冲区生命周期管理与切片传递。
    - 适配 RenderTarget.hasDepth() 及纹理视图获取逻辑。
  - **窗口与平台抽象迁移（SDL 适配）**：
    - 移除对 GLFW 静态类的直接依赖，鼠标位置改由 Minecraft.mouseHandler 获取，时间基准使用纳秒级时间源，全局按键与鼠标点击判断完全适配 26.3 的 InputConstants.isKeyDown(key)。
  - **GuiGraphicsExtractor 渲染拦截**：
    - 将 DrawContextMixin 中的 lit 与 litSprite 拦截切面全量对齐至 26.3 的 com.mojang.renderpearl.api.pipeline.RenderPipeline 描述符。
  - **继承 26.2-ev 全部扩展特性**：
    - 完整保留 Tooltip 悬浮提示框毛玻璃化（Layer 14）。
    - 完整保留 	ooltipOpacity 不透明度调节滑动条与配置持久化。
    - 完整保留配置界面全汉化与双语支持（zh_cn.json / n_us.json）。

---

## 26.2-ev 原修改日志：
- **Tooltip 悬浮提示框毛玻璃化（26.2 支持）**：
  - 在 ReGlassConfig.Features 及 ReGlassSettingsIO 中新增 	ooltips 开关支持，并在配置界面增加对应切换按钮。
  - 在 DrawContextMixin 中拦截并替换原版 	ooltip/background 与 	ooltip/frame 精灵图，将原版矩形紫边提示框重构为具有抗锯齿圆角、深色微透与菲涅尔高光的流体毛玻璃面板（Layer 14）。
- **Tooltip 不透明度调节与配置界面全汉化（双语支持）**：
  - 新增 	ooltipOpacity 提示框不透明度配置与滑动条（0.0 ~ 1.0），可随心调节深浅以确保提示文字清晰可见。
  - 在 DrawContextMixin 中动态链接 	ooltipOpacity 渲染背景。
  - 重构 ReGlassConfigScreen 与 MappedSlider，为所有分类、开关按钮与滑动调节项增加语言键，提供完整的简体中文（zh_cn.json）与英文（n_us.json）本地化。

> 以下是原README：

# ReGlass By ReStudio

### Liquid Glass Implemented On Your Favorite Pixelated And Cubic Game

<img width="669" height="422" alt="Screenshot 2025-10-20 113022" src="https://github.com/user-attachments/assets/d5c99347-c8cd-4c21-a430-54a0499c5f0f" />


ReGlass Is Meant To Be An API For Any Minecraft Mod.

### Features
- Easy, Customizable, And Fast Glass Rendering API.
- Highly Optimized, Almost Vanilla Performance (For Dedicated GPU PCs).
- Some Minecraft UI Redesigns.

### Building:
- ./gradlew build builds the 26.3 Fabric target.