package dev.cheatlab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class CheatLabScreen extends Screen {
    private static final int GREEN = 0xFF70EBA6;
    private static final int WHITE = 0xFFF0F4F7;
    private static final int MUTED = 0xFF8D9AAA;
    private static final String[] TABS = {"Combat", "Movement", "Visual", "HUD"};
    private String tab = "Combat";
    private long start = System.nanoTime();

    public CheatLabScreen() { super(Text.literal("CheatLab")); }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c, mx, my, delta);
        int x = width / 2 - 225, y = height / 2 - 145;
        double pulse = (Math.sin((System.nanoTime() - start) / 450000000.0) + 1) / 2;
        rounded(c, x - 2, y - 2, 454, 294, 0x5536D98B, 0x8870EBA6);
        rounded(c, x, y, 450, 290, 0xF20B1018, 0xFF70EBA6);
        rounded(c, x + 12, y + 48, 112, 228, 0xD9111822, 0xFF263644);
        rounded(c, x + 134, y + 48, 304, 228, 0xC9141D28, 0xFF334451);
        c.drawTextWithShadow(textRenderer, Text.literal("CHEATLAB"), x + 19, y + 16, GREEN);
        c.drawTextWithShadow(textRenderer, Text.literal("FABRIC 1.20.4  /  CLIENT"), x + 285, y + 17, MUTED);
        c.fill(x + 14, y + 39, x + 436, y + 40, 0x334D6570);
        for (int i = 0; i < TABS.length; i++) {
            int ty = y + 62 + i * 43;
            boolean active = tab.equals(TABS[i]);
            if (active) rounded(c, x + 19, ty - 8, 98, 29, 0xD52B7652, GREEN);
            c.drawTextWithShadow(textRenderer, Text.literal(TABS[i]), x + 31, ty, active ? WHITE : MUTED);
        }
        c.drawTextWithShadow(textRenderer, Text.literal(tab.toUpperCase()), x + 151, y + 62, GREEN);
        int row = y + 95;
        if (tab.equals("Combat")) {
            module(c, x, row, "CPS Counter", "Live left / right click counter", "HUD", true);
            module(c, x, row + 49, "Keystrokes", "Live WASD key display", "HUD", CheatLabClient.keystrokes);
            module(c, x, row + 98, "Armor Status", "Equipped armor information", "HUD", CheatLabClient.armorStatus);
        } else if (tab.equals("Movement")) {
            module(c, x, row, "Auto Sprint", "Sprint while moving forward", "CLIENT", CheatLabClient.autoSprint);
        } else if (tab.equals("Visual")) {
            module(c, x, row, "Fullbright", "Increase gamma in dark areas", "CLIENT", CheatLabClient.fullbright);
        } else {
            module(c, x, row, "FPS Counter", "Current frame rate", "HUD", CheatLabClient.fpsCounter);
            module(c, x, row + 49, "Coordinates", "Player block position", "HUD", CheatLabClient.coordinates);
            module(c, x, row + 98, "Watermark", "CheatLab HUD label", "HUD", CheatLabClient.watermark);
        }
        int dot = 0xFF000000 | ((int)(75 + pulse * 20) << 16) | ((int)(190 + pulse * 35) << 8) | 130;
        circle(c, x + 430, y + 22, 4 + pulse, dot);
        c.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  /  MENU"), x + 18, y + 269, MUTED);
        super.render(c, mx, my, delta);
    }

    private void module(DrawContext c, int x, int y, String name, String desc, String tag, boolean on) {
        int left = x + 147;
        rounded(c, left, y - 8, 286, 42, 0xB91B2631, on ? 0xFF4A9C72 : 0xFF394653);
        c.drawTextWithShadow(textRenderer, Text.literal(name), left + 11, y, WHITE);
        c.drawTextWithShadow(textRenderer, Text.literal(desc), left + 11, y + 14, MUTED);
        c.drawTextWithShadow(textRenderer, Text.literal(tag), left + 205, y + 3, MUTED);
        rounded(c, left + 238, y + 2, 39, 19, on ? 0xFF347B59 : 0xFF394451, on ? GREEN : 0xFF65717D);
        circle(c, left + (on ? 267 : 248), y + 11, 6, WHITE);
        c.drawTextWithShadow(textRenderer, Text.literal(on ? "ON" : "OFF"), left + 207, y + 20, on ? GREEN : MUTED);
    }

    private static void rounded(DrawContext c, int x, int y, int w, int h, int fill, int edge) {
        int r = Math.min(9, Math.min(w / 2, h / 2));
        for (int yy = 0; yy < h; yy++) {
            int inset = 0;
            if (yy < r) inset = (int)Math.ceil(r - Math.sqrt(r*r - (r-yy)*(r-yy)));
            else if (yy >= h-r) {
                int d = yy - (h-r-1);
                inset = (int)Math.ceil(r - Math.sqrt(r*r - (r-d)*(r-d)));
            }
            c.fill(x + inset, y + yy, x + w - inset, y + yy + 1, fill);
        }
        c.fill(x + r, y, x + w - r, y + 1, edge);
    }

    private static void circle(DrawContext c, int x, int y, double r, int color) {
        for (int dy = -(int)r; dy <= (int)r; dy++) {
            int dx = (int)Math.floor(Math.sqrt(Math.max(0, r*r-dy*dy)));
            c.fill(x-dx, y+dy, x+dx+1, y+dy+1, color);
        }
    }

    private boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x+w && my >= y && my <= y+h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(mx, my, button);
        int x = width/2-225, y = height/2-145, row = y+95;
        for (int i=0;i<TABS.length;i++) {
            if (hit(mx,my,x+19,y+54+i*43,98,29)) { tab=TABS[i]; return true; }
        }
        int left=x+147;
        if (tab.equals("Combat")) {
            if (hit(mx,my,left,row+41,286,42)) CheatLabClient.keystrokes=!CheatLabClient.keystrokes;
            else if (hit(mx,my,left,row+90,286,42)) CheatLabClient.armorStatus=!CheatLabClient.armorStatus;
            else return super.mouseClicked(mx,my,button);
            return true;
        }
        if (tab.equals("Movement") && hit(mx,my,left,row-8,286,42)) {
            CheatLabClient.autoSprint=!CheatLabClient.autoSprint; return true;
        }
        if (tab.equals("Visual") && hit(mx,my,left,row-8,286,42)) {
            CheatLabClient.setFullbright(client,!CheatLabClient.fullbright); return true;
        }
        if (tab.equals("HUD")) {
            if (hit(mx,my,left,row-8,286,42)) CheatLabClient.fpsCounter=!CheatLabClient.fpsCounter;
            else if (hit(mx,my,left,row+41,286,42)) CheatLabClient.coordinates=!CheatLabClient.coordinates;
            else if (hit(mx,my,left,row+90,286,42)) CheatLabClient.watermark=!CheatLabClient.watermark;
            else return super.mouseClicked(mx,my,button);
            return true;
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean shouldPause() { return false; }
}
