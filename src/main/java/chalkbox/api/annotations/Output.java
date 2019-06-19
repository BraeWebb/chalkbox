package chalkbox.api.annotations;

import chalkbox.api.collections.Collection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for methods which produces output.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Output {
    String stream() default "submissions";
    Class type() default Collection.class;
}
