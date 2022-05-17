package ai.classifai.core.entities;

public interface User {
    String getUserId();

    String getUserName();

    boolean isAdmin();

    boolean isSupervisor();

    boolean isAnnotator();
}
