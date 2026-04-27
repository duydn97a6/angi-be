package vn.angi.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.angi.user.dto.UserPreferencesDto;
import vn.angi.user.dto.UserResponse;
import vn.angi.user.entity.User;
import vn.angi.user.entity.UserPreferences;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isOnboarded", expression = "java(user.getIsOnboarded() != null && user.getIsOnboarded())")
    @Mapping(target = "preferences", source = "prefs")
    UserResponse toResponse(User user, UserPreferences prefs);

    UserPreferencesDto toDto(UserPreferences prefs);
}
