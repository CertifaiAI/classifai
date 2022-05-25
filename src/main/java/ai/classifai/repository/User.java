package ai.classifai.repository;

import ai.classifai.core.entities.UserEntity;
import ai.classifai.data.enumeration.UserType;

public class User implements UserEntity {
    private final String userId;
    private final String userName;
    private final UserType userType;

    User(String userId, String userName, UserType userType) {
        this.userId = userId;
        this.userName = userName;
        this.userType = userType;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public UserType getUserType() {
        return userType;
    }
}
