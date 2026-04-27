package vn.angi.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePreferencesRequest(
        String region,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal officeLat,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        BigDecimal officeLng,

        String officeAddress,

        @Min(100) @Max(10000)
        Integer searchRadiusMeters,

        String dietType,

        List<String> excludedFoods,
        List<String> favoriteCuisines,

        @Min(0)
        Integer budgetMin,

        @Min(0)
        Integer budgetMax,

        Boolean prefersDelivery,

        @Min(5) @Max(120)
        Integer maxDeliveryTimeMin
) {}
