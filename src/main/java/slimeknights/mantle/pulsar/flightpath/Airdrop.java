package slimeknights.mantle.pulsar.flightpath;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default annotation for Flightpath subscriptions.
 *
 * We don't use @Subscriber, as too many frameworks use it (which may lead to import hell)
 *
 * @author Arkan <arkan@drakon.io>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Airdrop {

}
