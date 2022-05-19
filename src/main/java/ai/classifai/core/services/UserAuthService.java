package ai.classifai.core.services;

import ai.classifai.core.entities.UserEntity;

public interface UserAuthService {
    UserEntity authenticateUser(String token);
}
