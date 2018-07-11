package common;
import java.io.Serializable;

public class FileInfo implements Serializable {
    public enum Operation {GET_FILE, PUT_FILE, DELETE_FILE, LIST_FILES}

    private String fileName;
    private long fileSize;
    private Operation operation;

    public FileInfo(String fileName, long fileSize, Operation operation) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.operation = operation;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Operation getOperation() {
        return operation;
    }
}
