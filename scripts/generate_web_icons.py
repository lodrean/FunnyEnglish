#!/usr/bin/env python3
"""
Generate SoToSpeak web icons (favicon, apple-touch-icon, manifest icons).
"""

from pathlib import Path
from PIL import Image, ImageDraw

PUBLIC_DIR = Path(__file__).resolve().parent.parent / "admin-web" / "public"

BACKGROUND = (0xEE, 0xF3, 0xFF, 0xFF)
MIC_COLOR = (0xFF, 0x9F, 0x6B, 0xFF)
WAVE_COLORS = [(0x5B, 0x8D, 0xEF, 0xFF), (0x9B, 0x7E, 0xDE, 0xFF), (0xFF, 0x9F, 0x6B, 0xFF)]


def draw_icon(size: int) -> Image.Image:
    """Draw the SoToSpeak icon at the requested pixel size."""
    img = Image.new("RGBA", (size, size), BACKGROUND)
    draw = ImageDraw.Draw(img)
    scale = size / 108.0

    def s(v: float) -> float:
        return v * scale

    draw.ellipse([s(46), s(12), s(62), s(28)], fill=MIC_COLOR)
    draw.rounded_rectangle([s(46), s(24), s(62), s(58)], radius=s(8), fill=MIC_COLOR)
    draw.rounded_rectangle([s(50), s(58), s(58), s(80)], radius=s(2), fill=MIC_COLOR)

    bars = [
        (40, 88, 46, 96, WAVE_COLORS[0]),
        (50, 84, 58, 96, WAVE_COLORS[1]),
        (62, 88, 68, 96, WAVE_COLORS[2]),
    ]
    for x1, y1, x2, y2, color in bars:
        draw.rounded_rectangle(
            [s(x1), s(y1), s(x2), s(y2)],
            radius=s((x2 - x1) / 2),
            fill=color,
        )

    return img


def main() -> None:
    PUBLIC_DIR.mkdir(parents=True, exist_ok=True)

    # favicon.ico: 32x32 (standard browser favicon size)
    favicon_path = PUBLIC_DIR / "favicon.ico"
    draw_icon(32).save(favicon_path, format="ICO", sizes=[(32, 32)])
    print(f"Generated {favicon_path}")

    # apple-touch-icon.png: 180x180
    apple = draw_icon(180)
    apple_path = PUBLIC_DIR / "apple-touch-icon.png"
    apple.save(apple_path, "PNG")
    print(f"Generated {apple_path}")

    # logo-icon-192.png: 192x192
    icon192 = draw_icon(192)
    icon192_path = PUBLIC_DIR / "logo-icon-192.png"
    icon192.save(icon192_path, "PNG")
    print(f"Generated {icon192_path}")

    # app-icon-512.png: 512x512
    icon512 = draw_icon(512)
    icon512_path = PUBLIC_DIR / "app-icon-512.png"
    icon512.save(icon512_path, "PNG")
    print(f"Generated {icon512_path}")


if __name__ == "__main__":
    main()
