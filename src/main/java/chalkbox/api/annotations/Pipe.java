package chalkbox.api.annotations;

import chalkbox.api.collections.Collection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link Processor} method to perform a transformation on a data structure.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Pipe {
    String stream() default "submissions";
    Class type() default Collection.class;
}
