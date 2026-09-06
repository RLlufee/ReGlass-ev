package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
import restudio.reglass.client.runtime.ReGlassAnim;

public final class LiquidGlassUniforms {

    private static final LiquidGlassUniforms INSTANCE = new LiquidGlassUniforms();
    public static final int MAX_WIDGETS = 64;
    public static final int MAX_BLUR_LEVELS = 5;

    public static LiquidGlassUniforms get() { return INSTANCE; }

    private final GpuBuffer samplerInfo;
    private final GpuBuffer customUniforms;
    private final GpuBuffer widgetInfo;
    private final GpuBuffer bgConfig;

    private final List<LiquidGlassGuiElementRenderState> widgets = new ArrayList<>();
    private boolean screenWantsBlur = false;

    private List<Integer> usedBlurRadiiOrdered = new ArrayList<>();
    private final HashMap<Integer, Integer> blurRadiusToIndex = new HashMap<>();
    private static final int MAX_LAYERS = 16;

    private static final class FadeState {
        float hover;
        float focus;
    }

    private final HashMap<Long, FadeState> fades = new HashMap<>();
    private double dtSeconds = 0.0;

    private LiquidGlassUniforms() {
        samplerInfo = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo", 130, 16);

        Std140SizeCalculator calc = new Std140SizeCalculator();
        calc.putFloat();
        calc.align(16);
        calc.putVec4();
        calc.putFloat();
        calc.align(16);
        calc.putVec3();
        calc.align(16);
        calc.putVec4();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        calc.putFloat();
        int customUniformsSize = calc.get();
        customUniforms = RenderSystem.getDevice().createBuffer(() -> "reglass CustomUniforms", 130, customUniformsSize);

        int widgetInfoSize = 16 + MAX_LAYERS * 16 + MAX_WIDGETS * (16 * 12);
        widgetInfo = RenderSystem.getDevice().createBuffer(() -> "reglass WidgetInfo", 130, widgetInfoSize);

        Std140SizeCalculator bcalc = new Std140SizeCalculator();
        bcalc.putFloat();
        bcalc.putFloat();
        bcalc.putVec2();
        int bgConfigSize = bcalc.get();
        bgConfig = RenderSystem.getDevice().createBuffer(() -> "reglass BgConfig", 130, bgConfigSize);
    }

    public void beginFrame(double dtSeconds) {
        widgets.clear();
        screenWantsBlur = false;
        usedBlurRadiiOrdered.clear();
        blurRadiusToIndex.clear();
        this.dtSeconds = Math.max(0.0, dtSeconds);
    }

    public boolean screenWantsBlur() { return this.screenWantsBlur; }

    public void setScreenWantsBlur(boolean wantsBlur) { this.screenWantsBlur = wantsBlur; }

    public void uploadSharedUniforms() {
        Minecraft mc = Minecraft.getInstance();
        int outW = mc.gameRenderer.mainRenderTarget().width;
        int outH = mc.gameRenderer.mainRenderTarget().height;

        try (var map = samplerInfo.map(false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putVec2((float) outW, (float) outH);
            b.putVec2((float) outW, (float) outH);
        }

        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(mc.getWindow().handle(), mx, my);
        float scale = (float) mc.getWindow().getGuiScale();
        int fbH = mc.gameRenderer.mainRenderTarget().height;

        float time = (float) GLFW.glfwGetTime();
        ReGlassConfig config = ReGlassConfig.INSTANCE;

        try (var map = customUniforms.map(false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat(time);
            b.align(16);
            float x = (float) (mx[0] * scale);
            float y = fbH - (float) (my[0] * scale);
            b.putVec4(new Vector4f(x, y, 0f, 0f));
            b.putFloat(this.screenWantsBlur ? 1.0f : 0.0f);
            b.align(16);
            var dir2 = config.rimLight.direction();
            b.putVec3(new Vector3f(dir2.x, dir2.y, 0.0f));
            b.align(16);
            int rc = config.rimLight.color();
            b.putVec4(ARGB.red(rc) / 255f, ARGB.green(rc) / 255f, ARGB.blue(rc) / 255f, config.rimLight.intensity());
            b.putFloat(config.pixelEpsilon);
            b.putFloat(ReGlassAnim.INSTANCE.debugStep());
            b.putFloat(config.features.pixelatedGrid ? 1.0f : 0.0f);
            b.putFloat(ReGlassAnim.INSTANCE.pixelatedGridSize());
            b.putFloat(ReGlassAnim.INSTANCE.hoverScalePx());
            b.putFloat(ReGlassAnim.INSTANCE.focusScalePx());
            b.putFloat(ReGlassAnim.INSTANCE.focusBorderWidthPx());
            b.putFloat(ReGlassAnim.INSTANCE.focusBorderIntensity());
            b.putFloat(ReGlassAnim.INSTANCE.focusBorderSpeed());
        }

        try (var map = bgConfig.map(false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat(ReGlassAnim.INSTANCE.shadowExpand());
            b.putFloat(ReGlassAnim.INSTANCE.shadowFactor());
            float s = (float) mc.getWindow().getGuiScale();
            b.putVec2(ReGlassAnim.INSTANCE.shadowOffsetX() * s, ReGlassAnim.INSTANCE.shadowOffsetY() * s);
        }
    }

    public void tryApplyBlur(GuiGraphicsExtractor context) {
        if (!screenWantsBlur) {
            context.blurBeforeThisStratum();
            screenWantsBlur = true;
        }
    }

    public void addWidget(LiquidGlassGuiElementRenderState element) {
        if (widgets.size() >= MAX_WIDGETS) return;
        widgets.add(element);
    }

    private static long rectKey(int x1, int y1, int x2, int y2) {
        long a = (((long) x1) & 0xFFFFFFFFL) | ((((long) y1) & 0xFFFFFFFFL) << 32);
        long b = (((long) x2) & 0xFFFFFFFFL) | ((((long) y2) & 0xFFFFFFFFL) << 32);
        long h = 1469598103934665603L;
        h ^= a; h *= 1099511628211L;
        h ^= b; h *= 1099511628211L;
        return h;
    }

    private float smoothToward(float current, float target, double dt, float tau) {
        if (tau <= 1e-5f) return target;
        float a = (float) (1.0 - Math.exp(-Math.max(0.0, dt) / Math.max(1e-4, tau)));
        float v = current + (target - current) * a;
        if (Math.abs(v - target) < 1e-4f) return target;
        return v;
    }

    public void uploadWidgetInfo() {
        Minecraft mc = Minecraft.getInstance();
        int fbH = mc.gameRenderer.mainRenderTarget().height;
        float scale = (float) mc.getWindow().getGuiScale();
        List<LiquidGlassGuiElementRenderState> renderWidgets = new ArrayList<>(widgets);
        renderWidgets.sort((a, b) -> Integer.compare(a.style().getLayer(), b.style().getLayer()));
        int[] layerStarts = new int[MAX_LAYERS];
        int[] layerCounts = new int[MAX_LAYERS];
        for (int i = 0; i < MAX_LAYERS; i++) {
            layerStarts[i] = -1;
        }
        for (int i = 0; i < renderWidgets.size(); i++) {
            LiquidGlassGuiElementRenderState w = renderWidgets.get(i);
            int layer = w.style().getLayer();
            if (layer < 0 || layer >= MAX_LAYERS) {
                continue;
            }
            if (layerStarts[layer] < 0) {
                layerStarts[layer] = i;
            }
            layerCounts[layer]++;
        }

        HashSet<Integer> requested = new HashSet<>();
        for (LiquidGlassGuiElementRenderState w : renderWidgets) {
            WidgetStyle s = w.style();
            requested.add(Math.max(1, s.getBlurRadius()));
        }
        List<Integer> sorted = requested.stream().sorted().toList();
        usedBlurRadiiOrdered = new ArrayList<>();
        for (int i = 0; i < sorted.size() && i < MAX_BLUR_LEVELS; i++) usedBlurRadiiOrdered.add(sorted.get(i));
        if (usedBlurRadiiOrdered.isEmpty()) usedBlurRadiiOrdered.add(ReGlassAnim.INSTANCE.blurRadiusInt());
        blurRadiusToIndex.clear();
        for (int i = 0; i < usedBlurRadiiOrdered.size(); i++) blurRadiusToIndex.put(usedBlurRadiiOrdered.get(i), i);

        try (var map = widgetInfo.map(false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat((float) renderWidgets.size());
            b.align(16);

            for (int i = 0; i < MAX_LAYERS; i++) {
                int start = layerStarts[i] < 0 ? 0 : layerStarts[i];
                b.putVec4((float) start, (float) layerCounts[i], 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    var w = renderWidgets.get(i);
                    float W = w.x2() - w.x1();
                    float H = w.y2() - w.y1();
                    float px = w.x1() * scale;
                    float pyTop = w.y1() * scale;
                    float pW = W * scale;
                    float pH = H * scale;
                    float cx = px + 0.5f * pW;
                    float cyTop = pyTop + 0.5f * pH;
                    float cyFB = (float) fbH - cyTop;
                    float rectX = cx - 0.5f * pW;
                    float rectY = cyFB - 0.5f * pH;
                    b.putVec4(rectX, rectY, pW, pH);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    var w = renderWidgets.get(i);
                    float rad = w.cornerRadius() * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    var style = renderWidgets.get(i).style();
                    int c = style.getTintColor();
                    b.putVec4(ARGB.red(c) / 255f, ARGB.green(c) / 255f, ARGB.blue(c) / 255f, style.getTintAlpha());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    b.putVec4(s.getRefThickness(), s.getRefFactor(), s.getRefDispersion(), s.getRefFresnelRange());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    b.putVec4(s.getRefFresnelHardness(), s.getRefFresnelFactor(), s.getGlareRange(), s.getGlareHardness());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    b.putVec4(s.getGlareConvergence(), s.getGlareOppositeFactor(), s.getGlareFactor(), s.getGlareAngleRad());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    b.putVec4(s.getSmoothing(), 0f, 0f, 0f);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    var w = renderWidgets.get(i);
                    ScreenRectangle sc = w.scissorArea();
                    if (sc != null) {
                        float sL = sc.left() * scale;
                        float sR = sc.right() * scale;
                        float sT = sc.top() * scale;
                        float sB = sc.bottom() * scale;
                        b.putVec4(sL, fbH - sB, sR, fbH - sT);
                    } else b.putVec4(0f, 0f, (float) mc.gameRenderer.mainRenderTarget().width, (float) mc.gameRenderer.mainRenderTarget().height);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    float sx = s.getShadowOffsetX() * scale;
                    float sy = s.getShadowOffsetY() * scale;
                    b.putVec4(s.getShadowExpand(), s.getShadowFactor(), sx, sy);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    int col = s.getShadowColor();
                    b.putVec4(ARGB.red(col) / 255f, ARGB.green(col) / 255f, ARGB.blue(col) / 255f, s.getShadowColorAlpha());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < renderWidgets.size()) {
                    WidgetStyle s = renderWidgets.get(i).style();
                    int radius = Math.max(1, s.getBlurRadius());
                    Integer idx = blurRadiusToIndex.get(radius);
                    if (idx == null) idx = 0;
                    var w = renderWidgets.get(i);
                    long key = s.hasFadeKey() ? s.getFadeKey() : rectKey(w.x1(), w.y1(), w.x2(), w.y2());
                    FadeState fs = fades.computeIfAbsent(key, k -> new FadeState());
                    fs.hover = smoothToward(fs.hover, Math.max(0f, Math.min(1f, w.hover())), dtSeconds, 0.12f);
                    fs.focus = smoothToward(fs.focus, Math.max(0f, Math.min(1f, w.focus())), dtSeconds, 0.18f);
                    b.putVec4((float) idx, fs.hover, fs.focus, (float) s.getLayer());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
        }
    }

    public int getCount() { return widgets.size(); }
    public double getFrameDeltaSeconds() { return dtSeconds; }
    public GpuBuffer getSamplerInfoBuffer() { return samplerInfo; }
    public GpuBuffer getCustomUniformsBuffer() { return customUniforms; }
    public GpuBuffer getWidgetInfoBuffer() { return widgetInfo; }
    public GpuBuffer getBgConfigBuffer() { return bgConfig; }
    public List<Integer> getUsedBlurRadiiOrdered() { return usedBlurRadiiOrdered; }
}
