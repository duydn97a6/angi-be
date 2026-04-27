package vn.angi.common.constant;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    // Auth
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";

    // Users
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String PREFERENCES_NOT_FOUND = "PREFERENCES_NOT_FOUND";

    // Restaurants
    public static final String RESTAURANT_NOT_FOUND = "RESTAURANT_NOT_FOUND";
    public static final String DISH_NOT_FOUND = "DISH_NOT_FOUND";

    // Recommendations
    public static final String RECOMMENDATION_NOT_FOUND = "RECOMMENDATION_NOT_FOUND";
    public static final String LLM_ERROR = "LLM_ERROR";

    // Meals
    public static final String MEAL_NOT_FOUND = "MEAL_NOT_FOUND";
}
