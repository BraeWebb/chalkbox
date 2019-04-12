package chalkbox.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a method that is executed once for a class prior to
 * any other class methods.
 *
 * The method is given a mapping of strings to strings which represents
 * the configuration file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prior {
}
