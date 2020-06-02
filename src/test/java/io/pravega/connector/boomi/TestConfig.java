package io.pravega.connector.boomi;

import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    public static final String DEFAULT_PROJECT_NAME = "test";

    public static Properties getProperties(String projectName, boolean failIfMissing) throws IOException {
        String propFile = projectName + ".properties";
        InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(propFile);
        if (in == null) {
            // Check in home directory
            File homeProps = new File(System.getProperty("user.home") + File.separator + propFile);
            if (homeProps.exists()) {
                in = new FileInputStream(homeProps);
            }
        }

        if (in == null) {
            Assumptions.assumeFalse(failIfMissing, projectName + ".properties missing (look in src/test/resources for template)");
            return null;
        }

        Properties props = new Properties();
        props.load(in);
        in.close();

        return props;
    }

    public static Properties getProperties() throws IOException {
        return getProperties(DEFAULT_PROJECT_NAME, true);
    }

    public static String getPropertyNotEmpty(String key) throws IOException {
        return getPropertyNotEmpty(getProperties(), key);
    }

    public static String getPropertyNotEmpty(Properties p, String key) {
        String value = p.getProperty(key);
        Assumptions.assumeTrue(value != null && !value.isEmpty(), String.format("The property %s is required", key));
        return value;
    }
}
