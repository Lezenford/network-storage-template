package ru.gb.storage.common.message;

import java.io.File;

public class FileRequestMessage extends Message {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
