package dev.cheatlab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CheatLabScreen extends Screen {
    private static final int GREEN = 0xFF70EBA6;
    private static final int WHITE = 0xFFF0F4F7;
    private static final int MUTED = 0xFF8D9AAA;
    private static final String[] TABS = {"Movement", "Visual", "HUD", "Info"};

    private static final Identifier PANEL =
            new Identifier("cheatlab", "textures/gui/panel.png");
    private static final Identifier CARD =
            new Identifier("cheatlab", "textures/gui/card.png");
    private static final Identifier SWITCH_ON =
            new Identifier("cheatlab", "textures/gui/switch-on.png");
    private static final Identifier SWITCH_OFF =
            new Identifier("cheatlab", "textures/gui/switch-off.png");

    private String tab = "HUD";
    private final long startedAt = System.nanoTime();

    public CheatLabScreen() {
        super(Text.literal("CheatLab"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int x = width / 2 - 240;
        int y = height / 2 - 160;
        double pulse = (Math.sin((System.nanoTime() - startedAt) / 460000000.0) + 1.0) / 2.0;

        context.drawTexture(PANEL, x, y, 480, 320, 0.0F, 0.0F, 480, 320, 480, 320);
        context.fill(x + 13, y + 54, x + 137, y + 303, 0x99101923);
        context.drawTextWithShadow(textRenderer, Text.literal("CHEATLAB"), x + 22, y + 17, GREEN);
        context.drawTextWithShadow(textRenderer, Text.literal("FABRIC 1.20.4  /  CLIENT"),
                x + 302, y + 18, MUTED);
        context.fill(x + 18, y + 43, x + 462, y + 44, 0x334D6570);

        for (int i = 0; i < TABS.length; i++) {
            int ty = y + 76 + i * 48;
            boolean selected = tab.equals(TABS[i]);
            if (selected) {
                context.fill(x + 19, ty - 9, x + 130, ty + 21, 0xCC286B4C);
                context.fill(x + 19, ty - 9, x + 22, ty + 21, GREEN);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(TABS[i]),
                    x + 31, ty, selected ? WHITE : MUTED);
        }

        context.drawTextWithShadow(textRenderer, Text.literal(tab.toUpperCase()),
                x + 153, y + 65, GREEN);
        drawModules(context, x, y);

        int dot = 0xFF000000 | ((int) (70 + pulse * 30) << 16)
                | ((int) (190 + pulse * 35) << 8) | 130;
        circle(context, x + 455, y + 23, 4 + pulse, dot);
        context.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  /  MENU"),
                x + 20, y + 296, MUTED);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawModules(DrawContext context, int x, int y) {
        String[] names;
        String[] descriptions;
        boolean[] states;

        switch (tab) {
            case "Movement" -> {
                names = new String[]{"Auto Sprint", "Zoom Key"};
                descriptions = new String[]{"Sprint while moving forward", "Hold C to zoom in"};
                states = new boolean[]{CheatLabClient.autoSprint, CheatLabClient.zoomEnabled};
            }
            case "Visual" -> {
                names = new String[]{"Fullbright"};
                descriptions = new String[]{"Increase brightness in dark areas"};
                states = new boolean[]{CheatLabClient.fullbright};
            }
            case "Info" -> {
                names = new String[]{"Keystrokes", "Armor Status", "World Clock"};
                descriptions = new String[]{"Live WASD key display", "Armor durability in percent", "In-game time and day"};
                states = new boolean[]{CheatLabClient.keystrokes, CheatLabClient.armorStatus,
                        CheatLabClient.worldClock};
            }
            default -> {
                names = new String[]{"FPS Counter", "Coordinates", "Ping Display", "Watermark"};
                descriptions = new String[]{"Current frame rate", "Player block position",
                        "Connection latency", "CheatLab HUD label"};
                states = new boolean[]{CheatLabClient.fpsCounter, CheatLabClient.coordinates,
                        CheatLabClient.pingDisplay, CheatLabClient.watermark};
            }
        }

        for (int i = 0; i < names.length; i++) {
            int rowY = y + 88 + i * 53;
            int cardX = x + 147;
            context.drawTexture(CARD, cardX, rowY, 315, 47,
                    0.0F, 0.0F, 286, 46, 286, 46);
            context.drawTextWithShadow(textRenderer, Text.literal(names[i]),
                    cardX + 12, rowY + 10, WHITE);
            context.drawTextWithShadow(textRenderer, Text.literal(descriptions[i]),
                    cardX + 12, rowY + 26, MUTED);
            Identifier toggle = states[i] ? SWITCH_ON : SWITCH_OFF;
            context.drawTexture(toggle, cardX + 258, rowY + 10, 48, 24,
                    0.0F, 0.0F, 56, 28, 56, 28);
        }
    }

    private static void circle(DrawContext context, int x, int y, double radius, int color) {
        for (int dy = -(int) radius; dy <= (int) radius; dy++) {
            int dx = (int) Math.floor(Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
            context.fill(x - dx, y + dy, x + dx + 1, y + dy + 1, color);
        }
    }

    private boolean hit(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(mouseX, mouseY, button);

        int x = width / 2 - 240;
        int y = height / 2 - 160;

        for (int i = 0; i < TABS.length; i++) {
            if (hit(mouseX, mouseY, x + 19, y + 67 + i * 48, 111, 30)) {
                tab = TABS[i];
                return true;
            }
        }

        int cardX = x + 147;
        int count = switch (tab) {
            case "Movement" -> 2;
            case "Visual" -> 1;
            case "Info" -> 3;
            default -> 4;
        };
        for (int i = 0; i < count; i++) {
            int rowY = y + 88 + i * 53;
            if (!hit(mouseX, mouseY, cardX, rowY, 315, 47)) continue;

            switch (tab) {
                case "Movement" -> {
                    if (i == 0) CheatLabClient.autoSprint = !CheatLabClient.autoSprint;
                    if (i == 1) CheatLabClient.zoomEnabled = !CheatLabClient.zoomEnabled;
                }
                case "Visual" -> CheatLabClient.setFullbright(client, !CheatLabClient.fullbright);
                case "Info" -> {
                    if (i == 0) CheatLabClient.keystrokes = !CheatLabClient.keystrokes;
                    if (i == 1) CheatLabClient.armorStatus = !CheatLabClient.armorStatus;
                    if (i == 2) CheatLabClient.worldClock = !CheatLabClient.worldClock;
                }
                default -> {
                    if (i == 0) CheatLabClient.fpsCounter = !CheatLabClient.fpsCounter;
                    if (i == 1) CheatLabClient.coordinates = !CheatLabClient.coordinates;
                    if (i == 2) CheatLabClient.pingDisplay = !CheatLabClient.pingDisplay;
                    if (i == 3) CheatLabClient.watermark = !CheatLabClient.watermark;
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
