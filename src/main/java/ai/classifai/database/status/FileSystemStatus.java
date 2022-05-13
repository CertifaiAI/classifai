package ai.classifai.database.status;

public enum FileSystemStatus {
    DID_NOT_INITIATED,
    ITERATING_FOLDER,
    DATABASE_UPDATING,
    DATABASE_UPDATED,
    DATABASE_NOT_UPDATED,
    ABORTED
}
