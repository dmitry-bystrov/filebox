package common;
import java.io.File;
import java.io.Serializable;

public class FileInfo implements Serializable {
    public enum Operation {GET_FILE, PUT_FILE, DELETE_FILE, RENAME_FILE, LIST_FILES}

    private String fileName;
    private String newFileName;
    private File selectedFile;
    private long fileSize;
    private Operation operation;

    public FileInfo(String fileName, long fileSize, Operation operation) {
        this.fileName = fileName;
        this.newFileName = fileName;
        this.selectedFile = null;
        this.fileSize = fileSize;
        this.operation = operation;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public Operation getOperation() {
        return operation;
    }
}
