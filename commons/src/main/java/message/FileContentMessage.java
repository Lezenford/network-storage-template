package message;

public class FileContentMessage extends Message{
    private long startPosition;
    private boolean last;
    private byte[] content;

    public byte[] getContent() {
        return content;
    }

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

    public void setContent(byte[] content) {
        this.content = content;
    }
}
