package vn.angi.common.constant;

public final class Constants {
    private Constants() {}

    public static final String API_PREFIX = "/api/v1";

    public static final int RECOMMENDATION_COUNT = 3;
    public static final long RECOMMENDATION_CACHE_MINUTES = 15;
    public static final long WEATHER_CACHE_SECONDS = 3600;
    public static final long USER_PREFERENCES_CACHE_DAYS = 1;

    public static final int DEFAULT_SEARCH_RADIUS_METERS = 1000;
    public static final int MAX_SEARCH_RADIUS_METERS = 5000;

    public static final String CATEGORY_SAFE = "safe";
    public static final String CATEGORY_FAMILIAR = "familiar";
    public static final String CATEGORY_DISCOVERY = "discovery";

    public static final String FEEDBACK_HAPPY = "happy";
    public static final String FEEDBACK_NEUTRAL = "neutral";
    public static final String FEEDBACK_SAD = "sad";

    public static final String MEAL_BREAKFAST = "breakfast";
    public static final String MEAL_LUNCH = "lunch";
    public static final String MEAL_SNACK = "snack";
    public static final String MEAL_DINNER = "dinner";
}
