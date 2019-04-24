package chalkbox.java.conformance.comparator;

import chalkbox.java.conformance.comparator.flags.Flag;
import chalkbox.java.conformance.comparator.flags.ListFlag;
import chalkbox.java.conformance.comparator.flags.SingularFlag;

import java.lang.reflect.Method;

public class MethodComparator extends CodeComparator<Method> {
    public MethodComparator(Method expected, Method actual) {
        super(expected, actual);
        name = "Method " + expected.getDeclaringClass().getName()
                + "." + expected.getName();
    }

    @Override
    protected void compare(Method expected, Method actual) {
        compareModifier(expected.getModifiers(), actual.getModifiers());

        Flag modifierFlag = new SingularFlag<>("Method return type does not match!",
                expected.getReturnType().getName(), actual.getReturnType().getName());
        flags.add(modifierFlag);

        ListFlag<String> exceptionFlag = new ListFlag<>("Thrown exceptions do not match!");
        for (Class parameter : expected.getExceptionTypes()) {
            exceptionFlag.addExpected(parameter.getName());
        }
        for (Class parameter : actual.getExceptionTypes()) {
            exceptionFlag.addActual(parameter.getName());
        }
        flags.add(exceptionFlag);

        ListFlag<String> parametersFlag = new ListFlag<>("Method parameters do not match!");
        for (Class parameter : expected.getParameterTypes()) {
            parametersFlag.addExpected(parameter.getName());
        }
        for (Class parameter : actual.getParameterTypes()) {
            parametersFlag.addActual(parameter.getName());
        }
        flags.add(parametersFlag);
    }
}
