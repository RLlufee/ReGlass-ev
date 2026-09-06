## 本fork修改：（仅修改26.2）
- **Tooltip 悬浮提示框毛玻璃化（26.2 支持）**：
  - 在 `ReGlassConfig.Features` 及 `ReGlassSettingsIO` 中新增 `tooltips` 开关支持，并在配置界面增加对应切换按钮。
  - 在 `DrawContextMixin` 中拦截并替换原版 `tooltip/background` 与 `tooltip/frame` 精灵图，将原版矩形紫边提示框重构为具有抗锯齿圆角、深色微透与菲涅尔高光的流体毛玻璃面板（Layer 14）。
- **Tooltip 不透明度调节与配置界面全汉化（双语支持）**：
  - 新增 `tooltipOpacity` 提示框不透明度配置与滑动条（0.0 ~ 1.0），可随心调节深浅以确保提示文字清晰可见。
  - 在 `DrawContextMixin` 中动态链接 `tooltipOpacity` 渲染背景。
  - 重构 `ReGlassConfigScreen` 与 `MappedSlider`，为所有分类、开关按钮与滑动调节项增加语言键，提供完整的简体中文（`zh_cn.json`）与英文（`en_us.json`）本地化。

> 以下是原README：

# ReGlass By ReStudio

### Liquid Glass Implemented On Your Favorite Pixelated And Cubic Game

<img width="669" height="422" alt="Screenshot 2025-10-20 113022" src="https://github.com/user-attachments/assets/d5c99347-c8cd-4c21-a430-54a0499c5f0f" />


ReGlass Is Meant To Be An API For Any Minecraft Mod.

### Features
- Easy, Customizable, And Fast Glass Rendering API.
- Highly Optimized, Almost Vanilla Performance (For Dedicated GPU PCs).
- Some Minecraft UI Redesigns.

### API Example:
```java
// Widget Based Dimensions
int cornerRadiusPx = 0.5f * Math.min(width, height); // Recommended Rounding
ReGlassApi.create(context).fromWidget(someWidget).cornerRadius(cornerRadiusPx).render();

// Custom Style 
customStyle = WidgetStyle.create()
        .tint(Formatting.GOLD.getColorValue(), 0.4f)
        .blurRadius(0).shadow(25f, 0.2f, 0f, 3f)
        .smoothing(.05f).shadowColor(0x000000, 1.0f);

// Static Based Rendering E.g. Called From Screen `render()`.
ReGlassApi.create(context).dimensions(10, 10, 100, 100).cornerRadius(cornerRadiusPx).style(customStyle).render();

// You Must Apply Blur
LiquidGlassUniforms.get().tryApplyBlur(context);


// Ready To Use Widget (Screen Usage Example):
boolean moveable = true; // Makes The Widget Draggable
addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, null).setMoveable(moveable));
```

### Keybinds:
- ReGlass keybinds are unbound by default and can be changed in Minecraft's Controls screen.

### Building:
- `./gradlew :26.1:runClient` runs the current 26.1 Fabric target.
- `./gradlew buildAll` builds every configured Stonecutter target.

## Contributing Is More Than Welcome!
Especially In The Minecraft UI Redesign Part, This Part Is Highly WIP And Needs a Lot of Work.

<img width="426" height="251" alt="Sun Set" src="https://github.com/user-attachments/assets/8231c19b-abea-42b2-807f-35c3f089d3c0" />

