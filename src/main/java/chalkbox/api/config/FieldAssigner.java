package chalkbox.api.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Interface to share common functionality of config fields assignment.
 *
 * Implements a custom version of the visitor pattern that uses reflection
 * to pick the method based on the field type in the first parameter of the
 * assign methods.
 */
public interface FieldAssigner {
    /**
     * Retrieve the default concrete implementation of the {@code FieldAssigner}
     * class.
     *
     * @param instance The instance containing the fields to assign values.
     * @return a concrete implementation of the {@code FieldAssigner}.
     */
    static FieldAssigner getInstance(Object instance) {
        return new FieldAssignerImpl(instance);
    }

    /**
     * Retrieve the method used to assign a value for the provided field.
     *
     * Searches for the matching assign method where the first parameter
     * is assignable to the type of the field.
     *
     * @param assigner The field assigner to select the method from.
     * @param field The field which needs to be assigned.
     * @return The appropriate 'assign' method for the field type.
     */
    static Method getMethod(FieldAssigner assigner, Field field) {
        for (Method method : assigner.getClass().getMethods()) {
            if (!method.getName().equals("assign")) {
                continue;
            }

            if (method.getParameterTypes()[0].isAssignableFrom(field.getType())) {
                return method;
            }
        }

        return null;
    }

    boolean assign(String type, Field field, String value);
    boolean assign(File type, Field field, String value);
    boolean assign(Enum<?> type, Field field, String value);
    boolean assign(int type, Field field, String value);
    boolean assign(boolean type, Field field, String value);
    boolean assign(List<?> type, Field field, String value);
    boolean assign(Object[] type, Field field, String value);
    boolean assign(ZipFile type, Field field, String value);
}
