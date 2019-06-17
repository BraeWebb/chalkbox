package chalkbox.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class that performs transformations on a data structure.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Processor {
    String description() default "No description provided for this processor";
    Class[] depends() default {};
    int threads() default 8;
}
