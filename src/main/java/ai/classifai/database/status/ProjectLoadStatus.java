package ai.classifai.database.status;

public enum ProjectLoadStatus {
    ERROR,
    LOADING,
    LOADED, //projectloader will have this status once create new project
    DID_NOT_INITIATED, //default value when ProjectLoader created from database
}
