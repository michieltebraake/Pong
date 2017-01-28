public class Buffer {
    private String buffer = "";
    private String[] latestCommand;

    public Buffer() {
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public void appendBuffer(String message) {
        buffer += message;
    }

    public String[] getLatestCommand() {
        return latestCommand;
    }

    public void setLatestCommand(String[] latestCommand) {
        this.latestCommand = latestCommand;
    }
}
