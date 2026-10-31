package dev.cheatlab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class CheatLabClient implements ClientModInitializer {
    public static boolean fullbright = false;
    public static boolean autoSprint = false;
    public static boolean coordinates = true;
    public static boolean fpsCounter = true;
    public static boolean watermark = true;
    private static double previousGamma = 1.0;
    private static KeyBinding menuKey;
    private static KeyBinding fullbrightKey;

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
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;
            int y = 8;
            if (watermark) {
                drawContext.fill(7, 6, 111, 22, 0xB810151D);
                drawContext.fill(7, 6, 9, 22, 0xFF65F59A);
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal("CHEATLAB"),
                        14, 10, 0xFF65F59A);
                y = 27;
            }
            if (fpsCounter) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("FPS: " + client.getCurrentFps()), 8, y, 0xFFE8EDF4);
                y += 11;
            }
            if (coordinates) {
                var pos = client.player.getBlockPos();
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()),
                        8, y, 0xFFE8EDF4);
            }
        });
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
