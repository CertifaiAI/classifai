package ai.classifai.core.entities;

import ai.classifai.data.enumeration.UserType;

public interface UserEntity {
    String getUserId();

    String getUserName();

    UserType getUserType();
}
