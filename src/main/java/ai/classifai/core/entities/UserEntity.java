package ai.classifai.core.entities;

import ai.classifai.data.UserType;

public interface UserEntity {
    String getUserId();

    String getUserName();

    UserType getUserType();
}
