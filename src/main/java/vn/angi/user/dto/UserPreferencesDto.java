package vn.angi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPreferencesDto(
        String region,
        BigDecimal officeLat,
        BigDecimal officeLng,
        String officeAddress,
        Integer searchRadiusMeters,
        String dietType,
        List<String> excludedFoods,
        List<String> favoriteCuisines,
        Integer budgetMin,
        Integer budgetMax,
        Boolean prefersDelivery,
        Integer maxDeliveryTimeMin
) {}
