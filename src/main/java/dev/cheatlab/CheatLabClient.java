package dev.cheatlab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CheatLabClient implements ClientModInitializer {
    public static boolean fullbright;
    public static boolean autoSprint;
    public static boolean coordinates = true;
    public static boolean fpsCounter = true;
    public static boolean watermark = true;
    public static boolean cpsDisplay = true;
    public static boolean keystrokes = true;
    public static boolean armorStatus = true;

    private static double previousGamma = 1.0;
    private static KeyBinding menuKey;
    private static KeyBinding fullbrightKey;
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean oldLeft;
    private static boolean oldRight;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatlab.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.cheatlab"));
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatlab.fullbright", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F,
                "category.cheatlab"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) client.setScreen(new CheatLabScreen());
            while (fullbrightKey.wasPressed()) setFullbright(client, !fullbright);
            if (autoSprint && client.player != null && client.currentScreen == null
                    && client.options.forwardKey.isPressed()
                    && !client.player.isSneaking() && !client.player.horizontalCollision) {
                client.player.setSprinting(true);
            }
            if (client.getWindow() != null) {
                boolean left = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
                boolean right = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
                long now = System.currentTimeMillis();
                if (left && !oldLeft) leftClicks.addLast(now);
                if (right && !oldRight) rightClicks.addLast(now);
                oldLeft = left;
                oldRight = right;
                trimClicks(now);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;
            int y = 8;
            if (watermark) {
                panel(drawContext, 7, 6, 112, 23, 0xD90D111A, 0xFF66F0A0);
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal("CHEATLAB"),
                        17, 13, 0xFF78F5AD);
                y = 34;
            }
            if (fpsCounter) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("FPS  " + client.getCurrentFps()), 9, y, 0xFFE7EDF5);
                y += 12;
            }
            if (coordinates) {
                var p = client.player.getBlockPos();
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("XYZ  " + p.getX() + "  " + p.getY() + "  " + p.getZ()),
                        9, y, 0xFFE7EDF5);
                y += 12;
            }
            if (cpsDisplay) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("CPS  " + getCps(true) + " / " + getCps(false)),
                        9, y, 0xFF78F5AD);
            }
            if (keystrokes) {
                int x = client.getWindow().getScaledWidth() - 67;
                int base = client.getWindow().getScaledHeight() - 57;
                keyBox(drawContext, client, x + 22, base, "W", client.options.forwardKey);
                keyBox(drawContext, client, x, base + 20, "A", client.options.leftKey);
                keyBox(drawContext, client, x + 22, base + 20, "S", client.options.backKey);
                keyBox(drawContext, client, x + 44, base + 20, "D", client.options.rightKey);
            }
            if (armorStatus) {
                int x = client.getWindow().getScaledWidth() - 55;
                int yy = 8;
                for (var stack : client.player.getArmorItems()) {
                    if (!stack.isEmpty()) {
                        drawContext.drawTextWithShadow(client.textRenderer,
                                Text.literal(stack.getName().getString() + " " + stack.getDamage()),
                                x, yy, 0xFFE7EDF5);
                        yy += 11;
                    }
                }
            }
        });
    }

    private static void keyBox(net.minecraft.client.gui.DrawContext ctx, MinecraftClient mc,
                               int x, int y, String label, KeyBinding key) {
        int color = key.isPressed() ? 0xE642A878 : 0xC9161D28;
        panel(ctx, x, y, x + 19, y + 17, color, key.isPressed() ? 0xFF78F5AD : 0xFF485463);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + 9, y + 5, 0xFFFFFFFF);
    }

    public static void panel(net.minecraft.client.gui.DrawContext ctx, int x1, int y1,
                             int x2, int y2, int color, int accent) {
        int radius = Math.min(6, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        for (int y = 0; y < y2 - y1; y++) {
            int inset = 0;
            if (y < radius) inset = (int) Math.ceil(radius - Math.sqrt(radius * radius - (radius - y) * (radius - y)));
            else if (y >= y2 - y1 - radius) {
                int dy = y - (y2 - y1 - radius - 1);
                inset = (int) Math.ceil(radius - Math.sqrt(radius * radius - (radius - dy) * (radius - dy)));
            }
            ctx.fill(x1 + inset, y1 + y, x2 - inset, y1 + y + 1, color);
        }
        ctx.fill(x1 + radius, y1, x2 - radius, y1 + 1, accent);
    }

    private static void trimClicks(long now) {
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.removeFirst();
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.removeFirst();
    }

    public static int getCps(boolean left) {
        trimClicks(System.currentTimeMillis());
        return (left ? leftClicks : rightClicks).size();
    }

    public static void setFullbright(MinecraftClient client, boolean enabled) {
        if (client == null) return;
        if (enabled && !fullbright) {
            previousGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(15.0);
        } else if (!enabled && fullbright) {
            client.options.getGamma().setValue(previousGamma);
        }
        fullbright = enabled;
    }
}
