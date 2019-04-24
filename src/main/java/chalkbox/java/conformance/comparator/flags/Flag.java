package chalkbox.java.conformance.comparator.flags;

public class Flag {
    protected String message;
    protected boolean flag;

    public Flag(String message) {
        this.message = message;
    }

    public Flag(String message, boolean flag) {
        this(message);
        this.flag = flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isSet() {
        return flag;
    }

    protected String getIndent(int indent) {
        return new String(new char[indent]).replace("\0", " ");
    }

    public String toString(int indent) {
        return getIndent(indent) + message;
    }
}
