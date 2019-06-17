package chalkbox.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A collector builds the initial data structures from command line parameters.
 *
 * A collector should have at least one {@link DataSet} method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collector {
    String description() default "No description provided for this collector";
}
