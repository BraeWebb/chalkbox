package chalkbox.java.conformance.comparator;

import chalkbox.java.conformance.comparator.flags.Flag;
import chalkbox.java.conformance.comparator.flags.ListFlag;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassComparator extends CodeComparator<Class> {
    public ClassComparator(Class expected, Class actual) {
        super(expected, actual);
        name = expected.isInterface() ? "interface " : "class ";
        name = name + expected.getName();
    }

    @Override
    protected void compare(Class expected, Class actual) {
        flags = new ArrayList<>();

        compareModifier(expected.getModifiers(), actual.getModifiers());

        try {
            ListFlag<String> fieldFlag = new ListFlag<>("Class fields do not match!");
            compareMembers(expected.getFields(), actual.getFields(), fieldFlag);
            flags.add(fieldFlag);

            ListFlag<String> methodFlag = new ListFlag<>("Class methods do not match!");
            compareMembers(expected.getMethods(), actual.getMethods(), methodFlag);
            flags.add(methodFlag);

            ListFlag<String> constructorFlag = new ListFlag<>("Class constructors do not match!");
            compareMembers(expected.getConstructors(), actual.getConstructors(), constructorFlag);
            flags.add(constructorFlag);
        } catch (NoClassDefFoundError ncd) {
            flags.add(new Flag("Failed to load fields: " + ncd.getMessage(), true));
        }

        Flag shouldBeInterface = new Flag("Expected class to be an interface");
        shouldBeInterface.setFlag(expected.isInterface() && !actual.isInterface());

        Flag shouldBeClass = new Flag("Class should not be an interface");
        shouldBeClass.setFlag(!expected.isInterface() && actual.isInterface());

        flags.add(shouldBeInterface);
        flags.add(shouldBeClass);
    }

    private void compareMembers(Member[] expected, Member[] actual,
                                ListFlag<String> flag) {
        Map<String, Member> expectedMembers = new HashMap<>();

        for (Member member : expected) {
            if (member.getDeclaringClass().getName().startsWith("java.")) {
                continue;
            }
            flag.addExpected(member.getName());
            expectedMembers.put(member.getName(), member);
        }
        for (Member member : actual) {
            if (member.getDeclaringClass().getName().startsWith("java.")) {
                continue;
            }
            flag.addActual(member.getName());
            Member expectedMember = expectedMembers.get(member.getName());
            if (expectedMember != null) {
                CodeComparator comparator = buildComparator(expectedMember, member);
                comparator.indent = indent + 4;
                subComparators.add(comparator);
            }
        }
    }

    private CodeComparator buildComparator(Member expected, Member actual) {
        if ((expected instanceof Field) && (actual instanceof Field)) {
            return new FieldComparator((Field) expected, (Field) actual);
        } else if ((expected instanceof Method) && (actual instanceof Method)) {
            return new MethodComparator((Method) expected, (Method) actual);
        } else if ((expected instanceof Constructor) && (actual instanceof Constructor)) {
            return new ConstructorComparator((Constructor) expected, (Constructor) actual);
        }

        throw new RuntimeException("Unknown comparator types");
    }
}
