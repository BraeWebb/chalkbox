package chalkbox.java.conformance.comparator;

import chalkbox.java.conformance.comparator.flags.ListFlag;

import java.lang.reflect.Constructor;

public class ConstructorComparator extends CodeComparator<Constructor> {
    public ConstructorComparator(Constructor expected, Constructor actual) {
        super(expected, actual);
        name = "Constructor " + expected.getDeclaringClass().getName()
                + "." + expected.getName();
    }

    @Override
    protected void compare(Constructor expected, Constructor actual) {
        compareModifier(expected.getModifiers(), actual.getModifiers());

        ListFlag<String> exceptionFlag = new ListFlag<>("Thrown exceptions do not match!");
        for (Class parameter : expected.getExceptionTypes()) {
            exceptionFlag.addExpected(parameter.getName());
        }
        for (Class parameter : actual.getExceptionTypes()) {
            exceptionFlag.addActual(parameter.getName());
        }
        flags.add(exceptionFlag);

        ListFlag<String> parametersFlag = new ListFlag<>("Constructor parameters do not match!");
        for (Class parameter : expected.getParameterTypes()) {
            parametersFlag.addExpected(parameter.getName());
        }
        for (Class parameter : actual.getParameterTypes()) {
            parametersFlag.addActual(parameter.getName());
        }
        flags.add(parametersFlag);
    }
}
