package ru.gb.storage.commons.message;

import java.util.Arrays;

public class FileContentMessage extends Message{

    private long startPosition;
    private byte[] content;
    private boolean last;



    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FileContentMessage{" +
                "startPosition=" + startPosition +
                ", content=" + Arrays.toString(content) +
                ", last=" + last +
                '}';
    }
}
