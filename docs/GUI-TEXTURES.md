# CheatLab GUI texture pipeline

The client GUI uses packaged PNG resources from:
- `assets/cheatlab/textures/gui/panel.png` (480x320)
- `assets/cheatlab/textures/gui/card.png` (286x46)
- `assets/cheatlab/textures/gui/switch-on.png` (56x28)
- `assets/cheatlab/textures/gui/switch-off.png` (56x28)

The editable source generator is `tools/generate-textures.html`. Open it in a browser and use the Download PNG buttons to export the Canvas artwork.

Current client utilities:
- Right Shift opens the menu.
- F toggles Fullbright.
- Hold C for Zoom (can be disabled in Movement).
- Auto Sprint, FPS, coordinates, ping, world clock, WASD, armor durability, watermark.

This build focuses on client-side quality-of-life and visual information. It does not automate attacks or aiming.
