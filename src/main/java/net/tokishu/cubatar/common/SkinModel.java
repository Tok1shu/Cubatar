package net.tokishu.cubatar.common;

/** Разбор query-параметра model: явное указание slim/classic поверх авто-определения. */
public final class SkinModel {

    private SkinModel() {}

    /** "slim"/"alex" → true, "classic"/"steve"/"wide" → false, иначе null (авто). */
    public static Boolean parse(String model) {
        if (model == null) return null;
        return switch (model.toLowerCase()) {
            case "slim", "alex" -> Boolean.TRUE;
            case "classic", "steve", "wide" -> Boolean.FALSE;
            default -> null;
        };
    }
}
