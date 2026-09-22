## 本版本（26.3-ev）移植与修改日志：
- **着色器 SPIR-V 语法规范化修复（解决客户端崩溃）**：
  - 修复 Minecraft 26.3 RenderPearl 渲染引擎在将 GLSL 编译为 SPIR-V 字节码时的编译报错（'location' : SPIR-V requires location for user input/output）。

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
