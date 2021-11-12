package ai.classifai.backend.action.rename;

public enum RenameDataErrorCode {
    RENAME_FAIL ("Fail to rename file"),
    FILENAME_EXIST ("Name exists"),
    FILENAME_CONTAIN_ILLEGAL_CHAR ("Contain illegal character"),
    RENAME_SUCCESS ("Rename success");

    private final String label;

    RenameDataErrorCode(String label) {
        this.label = label;
    }

    public String getErrorMessage() {
        return this.label;
    }
}
