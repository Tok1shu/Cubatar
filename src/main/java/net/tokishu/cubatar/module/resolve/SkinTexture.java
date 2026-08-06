package net.tokishu.cubatar.module.resolve;

/**
 * Текстуры из профиля Mojang: URL скина + модель + URL плаща. slim (Alex,
 * руки 3 вокселя) приходит явным полем {@code textures.SKIN.metadata.model
 * = "slim"}; отсутствие metadata означает classic (Steve, руки 4 вокселя).
 * capeUrl == null - у игрока нет плаща (или он его скрыл).
 */
public record SkinTexture(String url, boolean slim, String capeUrl) {}
