package dev.cheatlab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CheatLabClient implements ClientModInitializer {
    public static boolean fullbright;
    public static boolean autoSprint;
    public static boolean zoomEnabled = true;
    public static boolean coordinates = true;
    public static boolean fpsCounter = true;
    public static boolean watermark = true;
    public static boolean keystrokes = true;
    public static boolean armorStatus = true;
    public static boolean pingDisplay = true;
    public static boolean worldClock = true;

    private static final Identifier PANEL =
            new Identifier("cheatlab", "textures/gui/panel.png");
    private static final Identifier CARD =
            new Identifier("cheatlab", "textures/gui/card.png");

    private static double previousGamma = 1.0;
    private static int previousFov = 70;
    private static boolean zoomActive;
    private static KeyBinding menuKey;
    private static KeyBinding fullbrightKey;
    private static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatlab.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.cheatlab"));
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatlab.fullbright", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F,
                "category.cheatlab"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatlab.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C,
                "category.cheatlab"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) client.setScreen(new CheatLabScreen());
            while (fullbrightKey.wasPressed()) setFullbright(client, !fullbright);

            if (client.player == null || client.world == null) {
                restoreZoom(client);
                return;
            }

            if (autoSprint && client.currentScreen == null
                    && client.options.forwardKey.isPressed()
                    && !client.player.isSneaking()
                    && !client.player.horizontalCollision) {
                client.player.setSprinting(true);
            }

            boolean shouldZoom = zoomEnabled && zoomKey.isPressed()
                    && client.currentScreen == null;
            if (shouldZoom && !zoomActive) {
                previousFov = client.options.getFov().getValue();
                zoomActive = true;
            }
            if (shouldZoom) {
                client.options.getFov().setValue(Math.min(previousFov, 30));
            } else {
                restoreZoom(client);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.options.hudHidden) return;

            int y = 8;
            if (watermark) {
                drawTexture(drawContext, PANEL, 7, 6, 116, 25, 480, 320);
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("CHEATLAB"), 17, 13, 0xFF78F5AD);
                y = 36;
            }
            if (fpsCounter) {
                drawInfoLine(drawContext, client, "FPS  " + client.getCurrentFps(), y, 0xFFE7EDF5);
                y += 13;
            }
            if (coordinates) {
                var pos = client.player.getBlockPos();
                drawInfoLine(drawContext, client,
                        "XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ(),
                        y, 0xFFE7EDF5);
                y += 13;
            }
            if (pingDisplay && client.getNetworkHandler() != null) {
                var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (entry != null) {
                    drawInfoLine(drawContext, client, "PING  " + entry.getLatency() + " ms",
                            y, 0xFF78F5AD);
                }
            }
            if (worldClock) {
                long dayTime = Math.floorMod(client.world.getTimeOfDay(), 24000L);
                int hours = (int) ((dayTime / 1000L + 6L) % 24L);
                int minutes = (int) ((dayTime % 1000L) * 60L / 1000L);
                String clock = String.format("%02d:%02d", hours, minutes);
                drawInfoLine(drawContext, client,
                        "WORLD  " + clock + "  /  DAY " + (client.world.getTime() / 24000L + 1L),
                        y + 13, 0xFFB8C9D8);
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
                int x = client.getWindow().getScaledWidth() - 126;
                int yy = 8;
                for (var stack : client.player.getArmorItems()) {
                    if (!stack.isEmpty()) {
                        int remaining = stack.getMaxDamage() <= 0 ? 100
                                : Math.max(0, (stack.getMaxDamage() - stack.getDamage()) * 100
                                / stack.getMaxDamage());
                        drawTexture(drawContext, CARD, x - 5, yy - 3, 122, 16, 286, 46);
                        drawContext.drawTextWithShadow(client.textRenderer,
                                Text.literal(stack.getName().getString() + "  " + remaining + "%"),
                                x, yy, remaining < 20 ? 0xFFFF8B8B : 0xFFE7EDF5);
                        yy += 19;
                    }
                }
            }
        });
    }

    private static void drawInfoLine(net.minecraft.client.gui.DrawContext context,
                                     MinecraftClient client, String value, int y, int color) {
        drawTexture(context, CARD, 6, y - 3, Math.max(120, client.textRenderer.getWidth(value) + 18),
                16, 286, 46);
        context.drawTextWithShadow(client.textRenderer, Text.literal(value), 11, y, color);
    }

    private static void keyBox(net.minecraft.client.gui.DrawContext context, MinecraftClient client,
                               int x, int y, String label, KeyBinding key) {
        int color = key.isPressed() ? 0xFF78F5AD : 0xFFE7EDF5;
        drawTexture(context, CARD, x, y, 19, 17, 286, 46);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(label),
                x + 9, y + 5, color);
    }

    private static void drawTexture(net.minecraft.client.gui.DrawContext context,
                                    Identifier texture, int x, int y, int width, int height,
                                    int textureWidth, int textureHeight) {
        context.drawTexture(texture, x, y, width, height, 0.0F, 0.0F,
                textureWidth, textureHeight, textureWidth, textureHeight);
    }

    private static void restoreZoom(MinecraftClient client) {
        if (zoomActive && client != null) {
            client.options.getFov().setValue(previousFov);
            zoomActive = false;
        }
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
