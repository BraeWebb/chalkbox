package chalkbox.java.conformance.comparator.flags;

public class SingularFlag<T> extends Flag {
    private T expected;
    private T actual;

    public SingularFlag(String message, T expected, T actual) {
        super(message);
        this.expected = expected;
        this.actual = actual;
        this.flag = !expected.equals(actual);
    }

    public String toString(int indent) {
        StringBuilder builder = new StringBuilder(getIndent(indent));

        builder.append(message)
                .append(System.lineSeparator());
        builder.append(getIndent(indent)).append("Expected: ").append(expected)
                .append(System.lineSeparator())
                .append(getIndent(indent)).append("Actual:   ").append(actual)
                .append(System.lineSeparator());

        return builder.toString();
    }
}
