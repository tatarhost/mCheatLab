package dev.cheatlab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class CheatLabScreen extends Screen {
    private static final int GREEN = 0xFF65F59A;
    private static final int PANEL = 0xF00D1118;
    private static final int MUTED = 0xFF8995A5;
    private final String page;

    public CheatLabScreen() {
        this("Render");
    }

    private CheatLabScreen(String page) {
        super(Text.literal("CheatLab"));
        this.page = page;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 190;
        int top = this.height / 2 - 112;

        addDrawableChild(ButtonWidget.builder(Text.literal("RENDER"), b -> open("Render"))
                .dimensions(left + 12, top + 55, 82, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("MOVE"), b -> open("Movement"))
                .dimensions(left + 12, top + 82, 82, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD"), b -> open("HUD"))
                .dimensions(left + 12, top + 109, 82, 20).build());

        int x = left + 112;
        int y = top + 65;
        if (page.equals("Render")) {
            addToggle(x, y, "Fullbright", "Brightens dark areas", 0);
        } else if (page.equals("Movement")) {
            addToggle(x, y, "Auto Sprint", "Automatically sprint forward", 1);
        } else {
            addToggle(x, y, "Watermark", "Show CheatLab label", 2);
            addToggle(x, y + 39, "FPS Counter", "Display current frame rate", 3);
            addToggle(x, y + 78, "Coordinates", "Display block coordinates", 4);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("CLOSE"), b -> close())
                .dimensions(left + 270, top + 195, 92, 20).build());
    }

    private void open(String target) {
        if (client != null) client.setScreen(new CheatLabScreen(target));
    }

    private void addToggle(int x, int y, String name, String description, int id) {
        addDrawableChild(ButtonWidget.builder(toggleLabel(name, enabled(id)), b -> {
            setEnabled(id, !enabled(id));
            b.setMessage(toggleLabel(name, enabled(id)));
        }).dimensions(x + 150, y, 82, 20).build());
    }

    private static boolean enabled(int id) {
        return switch (id) {
            case 0 -> CheatLabClient.fullbright;
            case 1 -> CheatLabClient.autoSprint;
            case 2 -> CheatLabClient.watermark;
            case 3 -> CheatLabClient.fpsCounter;
            default -> CheatLabClient.coordinates;
        };
    }

    private void setEnabled(int id, boolean value) {
        switch (id) {
            case 0 -> CheatLabClient.setFullbright(client, value);
            case 1 -> CheatLabClient.autoSprint = value;
            case 2 -> CheatLabClient.watermark = value;
            case 3 -> CheatLabClient.fpsCounter = value;
            case 4 -> CheatLabClient.coordinates = value;
            default -> { }
        }
    }

    private static Text toggleLabel(String name, boolean state) {
        return Text.literal((state ? "ON  " : "OFF ") + name);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int left = this.width / 2 - 190;
        int top = this.height / 2 - 112;

        context.fill(left, top, left + 380, top + 224, PANEL);
        context.fill(left, top, left + 380, top + 2, GREEN);
        context.fill(left, top + 34, left + 96, top + 224, 0xB910151D);
        context.fill(left + 105, top + 45, left + 368, top + 181, 0x99182029);

        context.drawTextWithShadow(textRenderer, Text.literal("CHEATLAB"),
                left + 13, top + 13, GREEN);
        context.drawTextWithShadow(textRenderer, Text.literal("CLIENT MOD  /  1.20.4"),
                left + 112, top + 13, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal(page.toUpperCase()),
                left + 112, top + 49, GREEN);

        if (page.equals("Render")) {
            moduleText(context, left + 112, top + 88, "Fullbright", "Increase gamma in dark areas");
        } else if (page.equals("Movement")) {
            moduleText(context, left + 112, top + 88, "Auto Sprint", "Sprint while moving forward");
        } else {
            moduleText(context, left + 112, top + 88, "Watermark", "Show the client name on HUD");
            moduleText(context, left + 112, top + 127, "HUD widgets", "FPS counter and coordinates");
        }
        context.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  •  MENU"),
                left + 13, top + 202, MUTED);
        super.render(context, mouseX, mouseY, delta);
    }

    private void moduleText(DrawContext context, int x, int y, String name, String description) {
        context.drawTextWithShadow(textRenderer, Text.literal(name), x, y, 0xFFE8EDF4);
        context.drawTextWithShadow(textRenderer, Text.literal(description), x, y + 12, MUTED);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
