package chalkbox.api.annotations;

import chalkbox.api.collections.Collection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link Processor} method to execute some form of transformation
 * on all items being processed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GroupPipe {
    String stream() default "submissions";
    Class type() default Collection.class;
}
