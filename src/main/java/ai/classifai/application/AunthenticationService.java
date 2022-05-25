package ai.classifai.application;

import ai.classifai.core.entities.UserEntity;
import ai.classifai.core.services.UserAuthService;

public class AunthenticationService implements UserAuthService {

    @Override
    public UserEntity authenticateUser(String token) {
        return null;
    }
}
