package net.tokishu.cubatar.module.resolve;

import java.awt.image.BufferedImage;

/**
 * Результат резолва: картинка скина + модель, если она достоверно известна.
 * slim == null - модель неизвестна (скин пришёл по прямому URL, а не из
 * профиля Mojang), рендерам следует падать на эвристику по прозрачности.
 */
public record ResolvedSkin(BufferedImage image, Boolean slim) {}
