package io.dockstore.provision;

/**
 * Simple interface similar to https://github.com/qos-ch/slf4j/blob/master/slf4j-api/src/main/java/org/slf4j/spi/LocationAwareLogger.java
 * Bigger number means more output...which is kind of opposite of slf4j
 * @author gluu
 * @since 17/01/18
 */
public interface VerbosityLevel {
    int MINIMAL = 1;
    int NORMAL = 2;
}
