package chalkbox.java.conformance.comparator;

import chalkbox.java.conformance.comparator.flags.Flag;
import chalkbox.java.conformance.comparator.flags.SingularFlag;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class CodeComparator<T> {
    protected String name;
    protected int indent = 0;
    protected List<CodeComparator> subComparators = new ArrayList<>();
    protected List<Flag> flags = new ArrayList<>();

    public CodeComparator(T expected, T actual) {
        this.compare(expected, actual);
    }

    public String getName() {
        return name;
    }

    public boolean hasDifference() {
        for (Flag flag : flags) {
            if (flag.isSet()) {
                return true;
            }
        }
        for (CodeComparator comparator : subComparators) {
            if (comparator.hasDifference()) {
                return true;
            }
        }
        return false;
    }

    protected void compareModifier(int expected, int actual) {
        Flag modifierFlag = new SingularFlag<>("Modifier does not match!",
                Modifier.toString(expected), Modifier.toString(actual));
        modifierFlag.setFlag(expected != actual);
        flags.add(modifierFlag);
    }

    protected abstract void compare(T expected, T actual);

    private String getIndent() {
        return new String(new char[indent]).replace("\0", " ");
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(getIndent()).append(name)
                .append(System.lineSeparator());

        for (Flag flag : flags) {
            if (flag.isSet()) {
                builder.append(flag.toString(indent + 4))
                        .append(System.lineSeparator());
            }
        }

        for (CodeComparator comparator : subComparators) {
            if (comparator.hasDifference()) {
                builder.append(comparator.toString());
            }
        }

        return builder.toString();
    }
}
