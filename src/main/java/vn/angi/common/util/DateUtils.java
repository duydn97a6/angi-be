package vn.angi.common.util;

import vn.angi.common.constant.Constants;

import java.time.LocalTime;

public final class DateUtils {
    private DateUtils() {}

    public static String determineMealType(int hour) {
        if (hour >= 6 && hour < 10) return Constants.MEAL_BREAKFAST;
        if (hour >= 10 && hour < 15) return Constants.MEAL_LUNCH;
        if (hour >= 15 && hour < 18) return Constants.MEAL_SNACK;
        return Constants.MEAL_DINNER;
    }

    public static String currentMealType() {
        return determineMealType(LocalTime.now().getHour());
    }
}
