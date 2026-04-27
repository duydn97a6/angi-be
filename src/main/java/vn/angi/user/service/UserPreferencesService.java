package vn.angi.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.user.dto.UpdatePreferencesRequest;
import vn.angi.user.entity.UserPreferences;
import vn.angi.user.repository.UserPreferencesRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository repository;

    @Cacheable(value = "user-preferences", key = "#userId")
    public UserPreferences getByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PREFERENCES_NOT_FOUND, "Người dùng chưa thiết lập tuỳ chọn"));
    }

    public UserPreferences findOrInit(UUID userId) {
        return repository.findByUserId(userId).orElseGet(() ->
                repository.save(UserPreferences.builder().userId(userId).build()));
    }

    @Transactional
    @CacheEvict(value = "user-preferences", key = "#userId")
    public UserPreferences upsert(UUID userId, UpdatePreferencesRequest req) {
        UserPreferences prefs = repository.findByUserId(userId)
                .orElseGet(() -> UserPreferences.builder().userId(userId).build());

        if (req.region() != null) prefs.setRegion(req.region());
        if (req.officeLat() != null) prefs.setOfficeLat(req.officeLat());
        if (req.officeLng() != null) prefs.setOfficeLng(req.officeLng());
        if (req.officeAddress() != null) prefs.setOfficeAddress(req.officeAddress());
        if (req.searchRadiusMeters() != null) prefs.setSearchRadiusMeters(req.searchRadiusMeters());
        if (req.dietType() != null) prefs.setDietType(req.dietType());
        if (req.excludedFoods() != null) prefs.setExcludedFoods(req.excludedFoods());
        if (req.favoriteCuisines() != null) prefs.setFavoriteCuisines(req.favoriteCuisines());
        if (req.budgetMin() != null) prefs.setBudgetMin(req.budgetMin());
        if (req.budgetMax() != null) prefs.setBudgetMax(req.budgetMax());
        if (req.prefersDelivery() != null) prefs.setPrefersDelivery(req.prefersDelivery());
        if (req.maxDeliveryTimeMin() != null) prefs.setMaxDeliveryTimeMin(req.maxDeliveryTimeMin());

        return repository.save(prefs);
    }

    @Transactional
    @CacheEvict(value = "user-preferences", key = "#prefs.userId")
    public UserPreferences save(UserPreferences prefs) {
        return repository.save(prefs);
    }
}
