package ai.classifai.core.services;

public interface RouterService {
    void start();

    void stop();

    void enableDevelopmentCORS();

    void configureEndpoints();
}
