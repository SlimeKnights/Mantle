package mantle.pulsar.pulse;

import java.lang.annotation.*;

/**
 * Metadata annotation for IPulse implementations.
 *
 * @author Arkan <arkan@drakon.io>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Pulse {

    /**
     * @return This Pulses UID - Much like FML mods have mod IDs.
     */
    public String id();

    /**
     * @return This Pulses description, human-readable for config files. Can be blank.
     */
    public String description() default "";

    /**
     * @return Whether this Pulse is mandatory or not (true -> mandatory).
     */
    public boolean forced() default false;

    /**
     * @return Whether a configurable Pulse should be enabled by default. Ignored where forced = true.
     */
    public boolean defaultEnable() default true;

}
