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

    /**
     * Итоговая модель для рендера: null означает "решить по текстуре".
     * <p>
     * Явный параметр запроса главнее всего. Дальше - профиль Mojang, но
     * доверяем ему только когда он говорит slim: slim приходит явным
     * {@code metadata.model}, а вот classic - это ПРОСТО ОТСУТСТВИЕ metadata,
     * то есть "не указано". Скин при этом вполне может быть нарисован под
     * slim (руки 3 вокселя), и рендер classic-раскладкой съедает у задней
     * грани руки 2 пустые колонки - руки выглядят прорезанными. Поэтому
     * "classic" из профиля отдаём на проверку текстуре.
     */
    public static Boolean resolve(Boolean override, Boolean profileSlim) {
        if (override != null) return override;
        return Boolean.TRUE.equals(profileSlim) ? Boolean.TRUE : null;
    }
}
