package chalkbox.api.config;

/**
 * Interface of the chalkbox configuration settings.
 *
 * Typically chalkbox config settings are loaded from a box file.
 *
 * {@link ChalkboxConfig} provides the interface for retrieving configuration
 * values from a loaded configuration.
 */
public interface ChalkboxConfig {
    /**
     * Retrieve the value for the provided key value.
     *
     * @param key A key value to look for in the configuration.
     * @return The value or null if not assigned a value.
     */
    String value(String key);

    /**
     * Determine whether a configuration value has been set for the given key.
     *
     * @param key A key value to look for in the configuration.
     * @return True if a value has been assigned to the given key, else False.
     */
    boolean isSet(String key);
}
