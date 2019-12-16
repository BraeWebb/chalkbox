package chalkbox.api.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Default implementation of the field assigner interface.
 */
public class FieldAssignerImpl implements FieldAssigner {
    private Object instance;

    /**
     * Create a field assigner to assign to the given instance.
     *
     * @param instance Object to assign fields.
     */
    FieldAssignerImpl(Object instance) {
        this.instance = instance;
    }

    /**
     * Helper method to set a field.
     *
     * @return true if the field can be assigned, otherwise false.
     */
    private boolean assign(Field field, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean assign(String type, Field field, String value) {
        return assign(field, value);
    }

    @Override
    public boolean assign(File type, Field field, String value) {
        File file = Paths.get(value).toFile();

        if (!file.exists()) {
            System.err.println("Unable to find file: " + value);
            return false;
        }

        return assign(field, file);
    }

    @Override
    public boolean assign(Enum<?> type, Field field, String value) {
        Object enumValue = null;
        Object[] constants = field.getType().getEnumConstants();

        for (Object constant : constants) {
            if (constant.toString().equals(value)) {
                enumValue = constant;
                break;
            }
        }
        if (enumValue == null) {
            System.err.println("Invalid enum value for enum: " + field.getType());
            System.err.println("Value enum values are: " + Arrays.toString(constants));
            System.err.println("Found: " + value);
            return false;
        }

        return assign(field, enumValue);
    }

    @Override
    public boolean assign(int type, Field field, String value) {
        try {
            return assign(field, Integer.valueOf(value));
        } catch (NumberFormatException e) {
            System.err.println("Unable to cast " + value + " to integer");
            return false;
        }
    }

    @Override
    public boolean assign(boolean type, Field field, String value) {
        if (value.equalsIgnoreCase("true")) {
            assign(field, true);
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            assign(field, false);
            return true;
        }

        System.err.println("Unable to cast " + value + " to boolean");
        return false;
    }

    @Override
    public boolean assign(List type, Field field, String value) {
        return assign(field, Arrays.asList(value.split(",")));
    }

    @Override
    public boolean assign(Object[] type, Field field, String value) {
        return assign(field, value.split(","));
    }

    @Override
    public boolean assign(ZipFile type, Field field, String value) {
        try (ZipFile zipFile = new ZipFile(Paths.get(value).toFile())) {
            return assign(field, zipFile);
        } catch (ZipException e) {
            e.printStackTrace();
            System.err.println("Invalid zip file found");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IO Exception trying to read zip file");
        }
        return false;
    }
}
