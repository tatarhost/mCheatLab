package dev.cheatlab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class CheatLabScreen extends Screen {
    private static final int GREEN = 0xFF65F59A;
    private static final int PANEL = 0xE810141B;

    public CheatLabScreen() {
        super(Text.literal("CheatLab"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 2 - 48;

        this.addDrawableChild(ButtonWidget.builder(fullbrightLabel(), button -> {
            CheatLabClient.setFullbright(client, !CheatLabClient.fullbright);
            button.setMessage(fullbrightLabel());
        }).dimensions(centerX - 100, top, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(sprintLabel(), button -> {
            CheatLabClient.autoSprint = !CheatLabClient.autoSprint;
            button.setMessage(sprintLabel());
        }).dimensions(centerX - 100, top + 27, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> close())
                .dimensions(centerX - 100, top + 61, 200, 20).build());
    }

    private static Text fullbrightLabel() {
        return Text.literal("Fullbright: " + (CheatLabClient.fullbright ? "ON" : "OFF"));
    }

    private static Text sprintLabel() {
        return Text.literal("AutoSprint: " + (CheatLabClient.autoSprint ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int left = this.width / 2 - 125;
        int top = this.height / 2 - 82;
        context.fill(left, top, left + 250, top + 170, PANEL);
        context.fill(left, top, left + 250, top + 2, GREEN);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, top + 14, GREEN);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Right Shift — меню   |   F — Fullbright"),
                this.width / 2, top + 39, 0xFFB8C2CE);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
