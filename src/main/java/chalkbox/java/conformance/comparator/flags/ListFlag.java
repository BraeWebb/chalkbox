package chalkbox.java.conformance.comparator.flags;

import java.util.ArrayList;
import java.util.List;

public class ListFlag<T> extends Flag {
    private List<T> expected = new ArrayList<>();
    private List<T> actual = new ArrayList<>();

    public ListFlag(String message) {
        super(message);
    }

    public void addExpected(T item) {
        expected.add(item);
    }

    public void addActual(T item) {
        actual.add(item);
    }

    public boolean isSet() {
        for (T expect : expected) {
            if (!actual.contains(expect)) {
                return true;
            }
        }

        for (T actual : actual) {
            if (!expected.contains(actual)) {
                return true;
            }
        }

        return false;
    }

    public String toString(int indent) {
        StringBuilder builder = new StringBuilder(getIndent(indent));

        List<T> missing = new ArrayList<>();
        List<T> extra = new ArrayList<>();

        for (T expect : expected) {
            if (!actual.contains(expect)) {
                missing.add(expect);
            }
        }

        for (T actual : actual) {
            if (!expected.contains(actual)) {
                extra.add(actual);
            }
        }

        builder.append(message)
                .append(System.lineSeparator());
        builder.append(getIndent(indent)).append("Missing: ").append(missing)
                .append(System.lineSeparator());
        builder.append(getIndent(indent)).append("Extra:   ").append(extra)
                .append(System.lineSeparator());

        return builder.toString();
    }
}
