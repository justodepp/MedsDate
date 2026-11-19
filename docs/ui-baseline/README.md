# UI baseline

Reference screenshots of the app **before** the Material 3 Expressive work (branch `ui/baseline`, commit of `dev` after `chore/coil3`).
Compare against these after each `ui/*` branch.

- Device: emulator Pixel_10_Pro, API 37, 1280x2856 (stored at 720 px wide, JPEG q80), English system language.
- Debug build, 7 sample medicines (2 expired, 3 expiring soon, 3 valid; 3 with an image).
- Sets: `light/` (13 screens), `dark/` (10), `large-font/` (font scale 1.6, 4 screens).
- Not covered: reduced animations, TalkBack, tablets, landscape.

## Issues seen while capturing (input for `ui/*`)

1. **Dark theme, Home**: expired and expiring cards keep a light pastel background but the title and date use light text, so they are almost unreadable (`dark/01-home.jpg`). Same cause likely on Detail.
2. **Dark theme**: the top app bar is a bright green, the splash screen is white (flash on launch).
3. **"Expires today" is listed under "Expired medicines"** (item expired earlier today) and the card is yellow, not red. Stats counts it twice: Expired 2 + Expiring soon 3 + Valid 3 = 8, total 7 (`light/05-stats.jpg`).
4. **Not localized**: validation messages ("Name is required", "Expiry date must be in the future"); content description "Immagine" on the photo icon.
5. **Settings**: "Save Settings" does nothing, values are saved on every change.
