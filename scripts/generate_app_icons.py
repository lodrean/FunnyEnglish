#!/usr/bin/env python3
"""
Generate SoToSpeak app icon PNGs from the vector foreground design.

Creates legacy launcher icons for Android mipmap densities from the same
108dp design used in ic_launcher_foreground.xml: microphone + waveform.
The icon fits within the 66dp Android adaptive-icon safe zone with clear
space around the content.
"""

from pathlib import Path
from PIL import Image, ImageDraw

BASE_DIR = Path(__file__).resolve().parent.parent
OUT_DIRS = {
    "composeApp": BASE_DIR / "composeApp" / "src" / "androidMain" / "res",
    "app": BASE_DIR / "app" / "src" / "main" / "res",
}

# Legacy launcher icon sizes per density
SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Brand colors (RGBA)
BACKGROUND = (0xEE, 0xF3, 0xFF, 0xFF)
MIC_COLOR = (0xFF, 0x9F, 0x6B, 0xFF)
WAVE_COLORS = [(0x5B, 0x8D, 0xEF, 0xFF), (0x9B, 0x7E, 0xDE, 0xFF), (0xFF, 0x9F, 0x6B, 0xFF)]


def draw_icon(size: int) -> Image.Image:
    """Draw a square icon at the requested pixel size."""
    img = Image.new("RGBA", (size, size), BACKGROUND)
    draw = ImageDraw.Draw(img)

    # Design fits inside the 66dp Android adaptive-icon safe zone inside 108dp.
    scale = size / 108.0

    def s(v: float) -> float:
        return v * scale

    # Microphone head
    head_cx, head_cy, head_r = 54, 28, 7
    draw.ellipse(
        [s(head_cx - head_r), s(head_cy - head_r), s(head_cx + head_r), s(head_cy + head_r)],
        fill=MIC_COLOR,
    )

    # Microphone body (rounded rect)
    draw.rounded_rectangle(
        [s(47), s(28), s(61), s(52)],
        radius=s(7),
        fill=MIC_COLOR,
    )

    # Microphone base
    draw.rounded_rectangle(
        [s(51), s(52), s(57), s(66)],
        radius=s(2),
        fill=MIC_COLOR,
    )

    # Waveform bars below the microphone (larger, balanced)
    bars = [
        (42, 74, 48, 88, WAVE_COLORS[0]),
        (51, 70, 57, 88, WAVE_COLORS[1]),
        (60, 74, 66, 88, WAVE_COLORS[2]),
    ]
    for x1, y1, x2, y2, color in bars:
        draw.rounded_rectangle(
            [s(x1), s(y1), s(x2), s(y2)],
            radius=s((x2 - x1) / 2),
            fill=color,
        )

    return img


def main() -> None:
    for module, res_dir in OUT_DIRS.items():
        for folder, size in SIZES.items():
            out_dir = res_dir / folder
            out_dir.mkdir(parents=True, exist_ok=True)

            for name in ("ic_launcher.png", "ic_launcher_round.png"):
                out_path = out_dir / name
                icon = draw_icon(size)
                if name.endswith("_round.png"):
                    mask = Image.new("L", (size, size), 0)
                    mask_draw = ImageDraw.Draw(mask)
                    mask_draw.ellipse((0, 0, size, size), fill=255)
                    rounded = Image.new("RGBA", (size, size), BACKGROUND)
                    rounded.paste(icon, (0, 0), mask)
                    icon = rounded

                icon.save(out_path, "PNG")
                print(f"Generated {out_path}")


if __name__ == "__main__":
    main()
