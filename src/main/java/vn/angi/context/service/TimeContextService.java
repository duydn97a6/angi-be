package vn.angi.context.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class TimeContextService {

    public String determineMealType() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 10) {
            return "breakfast";
        } else if (hour >= 10 && hour < 15) {
            return "lunch";
        } else if (hour >= 15 && hour < 18) {
            return "snack";
        } else {
            return "dinner";
        }
    }

    public boolean isLunchTime() {
        return "lunch".equals(determineMealType());
    }

    public boolean isDinnerTime() {
        return "dinner".equals(determineMealType());
    }
}
