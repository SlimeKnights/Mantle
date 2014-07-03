package mantle.pulsar.pulse;

import java.lang.annotation.*;

/**
 * Annotates methods to be used for event handling.
 *
 * @author Arkan <arkan@drakon.io>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Handler
{}
