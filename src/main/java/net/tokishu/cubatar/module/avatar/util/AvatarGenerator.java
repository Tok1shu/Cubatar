package net.tokishu.cubatar.module.avatar.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;

@UtilityClass
public class AvatarGenerator {

    public static BufferedImage extractHeadIcon(BufferedImage rawSkin, int size) {
        if (rawSkin == null) return null;

        // 1. Принудительно конвертируем в ARGB (чтобы была прозрачность)
        BufferedImage skin = normalizeSkin(rawSkin);

        int skinWidth = skin.getWidth();
        float scale = skinWidth / 64f;
        int blockSize = Math.round(8 * scale);

        // Координаты лица
        int frontX = Math.round(8 * scale);
        int frontY = Math.round(8 * scale);

        // Координаты оверлея (шапки)
        int overlayX = Math.round(40 * scale);
        int overlayY = Math.round(8 * scale);
        int overlayBackX = Math.round(56 * scale);
        int overlayBackY = Math.round(8 * scale);

        // РЕШАЮЩИЙ МОМЕНТ: Проверяем, стоит ли рисовать оверлей
        // Если это старый скин (64x32) и слой шапки полностью залит (нет прозрачности),
        // значит Java прочитала его криво, либо это черный фон. Отключаем шапку.
        boolean isLegacy = (rawSkin.getHeight() == 32);
        boolean hasOverlay = shouldRenderOverlay(skin, overlayX, overlayY, blockSize, blockSize, isLegacy);

        int overlaySize = (int) (size * 1.08);
        int resultSize = overlaySize + 4;

        BufferedImage result = new BufferedImage(resultSize, resultSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        // Пиксель-арт сглаживание
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int centerX = resultSize / 2;
        int centerY = resultSize / 2;

        if (hasOverlay) {
            // Задник шапки (для объема)
            BufferedImage backOverlay = extractAndScale(skin, overlayBackX, overlayBackY, blockSize, blockSize, overlaySize);
            // Рисуем задник, только если он не сплошной квадрат (эвристика)
            if (hasVisiblePixels(backOverlay)) {
                backOverlay = darkenImage(backOverlay, 0.6f);
                g.drawImage(backOverlay, centerX - overlaySize / 2, centerY - overlaySize / 2, null);
            }

            // Лицо
            BufferedImage face = extractAndScale(skin, frontX, frontY, blockSize, blockSize, size);
            g.drawImage(face, centerX - size / 2, centerY - size / 2, null);

            // Перед шапки
            BufferedImage frontOverlay = extractAndScale(skin, overlayX, overlayY, blockSize, blockSize, overlaySize);
            g.drawImage(frontOverlay, centerX - overlaySize / 2, centerY - overlaySize / 2, null);
        } else {
            // Только лицо (для сломанных или лысых скинов)
            BufferedImage face = extractAndScale(skin, frontX, frontY, blockSize, blockSize, size);
            g.drawImage(face, centerX - size / 2, centerY - size / 2, null);
        }

        g.dispose();
        return result;
    }

    /**
     * Эвристика: проверяем, не является ли "шапка" просто черным квадратом
     */
    private static boolean shouldRenderOverlay(BufferedImage skin, int x, int y, int w, int h, boolean isLegacy) {
        BufferedImage crop = skin.getSubimage(x, y, w, h);

        int totalPixels = w * h;
        int solidPixels = 0;
        int transparentPixels = 0;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int alpha = (crop.getRGB(i, j) >> 24) & 0xFF;
                if (alpha == 0) {
                    transparentPixels++;
                } else if (alpha == 255) {
                    solidPixels++;
                }
            }
        }

        if (solidPixels == 0 && transparentPixels == totalPixels) return false;

        if (isLegacy && solidPixels == totalPixels) {
            return false;
        }

        return true;
    }

    private static BufferedImage normalizeSkin(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        int targetH = w;

        if (w == h && original.getType() == BufferedImage.TYPE_INT_ARGB) return original;

        BufferedImage normalized = new BufferedImage(w, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = normalized.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return normalized;
    }

    private static BufferedImage extractAndScale(BufferedImage skin, int x, int y, int w, int h, int targetSize) {
        BufferedImage extracted = skin.getSubimage(x, y, w, h);
        if (w == targetSize) return extracted;

        BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(extracted, 0, 0, targetSize, targetSize, null);
        g.dispose();
        return scaled;
    }

    private static BufferedImage darkenImage(BufferedImage img, float factor) {
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (int)(((argb >> 16) & 0xFF) * factor);
                int g = (int)(((argb >> 8) & 0xFF) * factor);
                int b = (int)((argb & 0xFF) * factor);

                res.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return res;
    }

    private static boolean hasVisiblePixels(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (((img.getRGB(x, y) >> 24) & 0xFF) > 0) return true;
            }
        }
        return false;
    }
}