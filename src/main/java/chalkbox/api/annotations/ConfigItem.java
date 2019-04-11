package chalkbox.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration option from the box config file.
 *
 * <p>If the key is empty (the default) the fields name will be used.
 * <p>The required key indicates if an error should be thrown when the configuration is missing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigItem {
    String key() default "";
    String description() default "";
    boolean required() default true;
}
