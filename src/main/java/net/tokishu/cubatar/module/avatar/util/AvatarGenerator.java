package net.tokishu.cubatar.module.avatar.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;

@UtilityClass
public class AvatarGenerator {

    /**
     * Создает 3D иконку головы с эффектом объёма в 3 слоя.
     * Задний слой (overlay затемненный), средний (базовая голова), передний (overlay).
     *
     * @param skin исходное изображение скина
     * @param size базовый размер головы
     * @return 3D изображение головы с эффектом глубины
     */
    public static BufferedImage extractHeadIcon(BufferedImage skin, int size) {
        if (skin == null) {
            throw new IllegalArgumentException("Skin image cannot be null");
        }

        int skinWidth = skin.getWidth();
        int skinHeight = skin.getHeight();

        // Вычисляем масштаб относительно стандартного размера 64x64
        float scale = skinWidth / 64f;

        // Размер одного блока головы в пикселях скина
        int blockSize = Math.round(8 * scale);

        // Координаты передней грани головы: (8, 8)
        int frontX = Math.round(8 * scale);
        int frontY = Math.round(8 * scale);

        // Координаты второго слоя (overlay/hat) передней грани: (40, 8)
        int overlayFrontX = Math.round(40 * scale);
        int overlayFrontY = Math.round(8 * scale);

        // Координаты второго слоя задней грани: (56, 8)
        int overlayBackX = Math.round(56 * scale);
        int overlayBackY = Math.round(8 * scale);

        // Проверяем наличие второго слоя
        boolean hasOverlay = (overlayFrontX + blockSize <= skinWidth) &&
                (overlayFrontY + blockSize <= skinHeight) &&
                hasVisiblePixels(skin, overlayFrontX, overlayFrontY, blockSize, blockSize);

        // Размер overlay слоя (на 6.25% больше, как в игре: 8.5/8 = 1.0625)
        int overlaySize = (int)(size * 1.08);

        // Итоговое изображение (квадрат с небольшим запасом для overlay)
        int resultSize = overlaySize + 4;
        BufferedImage result = new BufferedImage(resultSize, resultSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Центрируем изображение
        int centerX = resultSize / 2;
        int centerY = resultSize / 2;

        if (hasOverlay) {
            // Слой 1: Задний слой (задняя грань overlay затемненная на 20%)
            BufferedImage overlayBack = extractAndScale(skin, overlayBackX, overlayBackY, blockSize, blockSize, overlaySize);
            overlayBack = darkenImage(overlayBack, 0.6f);
            g.drawImage(overlayBack, centerX - overlaySize / 2, centerY - overlaySize / 2, null);

            // Слой 2: Базовая голова (передняя грань по центру)
            BufferedImage frontFace = extractAndScale(skin, frontX, frontY, blockSize, blockSize, size);
            g.drawImage(frontFace, centerX - size / 2, centerY - size / 2, null);

            // Слой 3: Передний слой (передняя грань overlay)
            BufferedImage overlayFront = extractAndScale(skin, overlayFrontX, overlayFrontY, blockSize, blockSize, overlaySize);
            g.drawImage(overlayFront, centerX - overlaySize / 2, centerY - overlaySize / 2, null);
        } else {
            // Только базовая голова по центру
            BufferedImage frontFace = extractAndScale(skin, frontX, frontY, blockSize, blockSize, size);
            g.drawImage(frontFace, centerX - size / 2, centerY - size / 2, null);
        }

        g.dispose();
        return result;
    }

    /**
     * Извлекает и масштабирует область из скина
     */
    private static BufferedImage extractAndScale(BufferedImage skin, int x, int y, int w, int h, int targetSize) {
        BufferedImage extracted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = extracted.createGraphics();
        g.drawImage(skin, 0, 0, w, h, x, y, x + w, y + h, null);
        g.dispose();

        if (w == targetSize) return extracted;

        BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(extracted, 0, 0, targetSize, targetSize, null);
        g.dispose();

        return scaled;
    }

    /**
     * Затемняет изображение для эффекта освещения
     */
    private static BufferedImage darkenImage(BufferedImage img, float factor) {
        BufferedImage darkened = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (int)(((rgb >> 16) & 0xFF) * factor);
                int g = (int)(((rgb >> 8) & 0xFF) * factor);
                int b = (int)((rgb & 0xFF) * factor);

                darkened.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return darkened;
    }

    /**
     * Проверяет наличие видимых пикселей в области
     */
    private static boolean hasVisiblePixels(BufferedImage image, int x, int y, int width, int height) {
        int maxX = Math.min(x + width, image.getWidth());
        int maxY = Math.min(y + height, image.getHeight());

        for (int py = y; py < maxY; py++) {
            for (int px = x; px < maxX; px++) {
                int alpha = (image.getRGB(px, py) >> 24) & 0xFF;
                if (alpha > 0) return true;
            }
        }
        return false;
    }
}