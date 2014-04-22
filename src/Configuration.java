import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Java Properties File Examples
 * http://www.mkyong.com/java/java-properties-file-examples/
 *
 * @author anatoly.prokofyev
 * @date 15.04.14.
 */
public class Configuration{
    private static Properties prop;
    private static final String DEFAULT_FILE_NAME = "configuration.properties";
    private static final String SYSTEM_PROPERTY_FILE_NAME = "configuration.file";
    private static final Logger LOGGER = Logger.getLogger(Configuration.class .getName());

    public Configuration() {
        init();
    }

    private synchronized void init() {
        if (prop == null) {
            prop = new Properties();
            read();
        }
    }

    private void read() {
        String fileName = System.getProperty(SYSTEM_PROPERTY_FILE_NAME, DEFAULT_FILE_NAME);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(fileName);
            prop.load(stream);
        } catch (FileNotFoundException e) {
            LOGGER.warning("Configuration file " + fileName + " is not found.");
        } catch (IOException e) {
            LOGGER.warning("IOException occurred: " + e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // suppress
                }
            }
        }
    }

    private String getSystemProperty(String name) {
        return System.getProperty(name);
    }

    public String getProperty(String name) {
        String systemProperty = getSystemProperty(name);
        return systemProperty != null ? systemProperty : prop.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        return value != null ? value : defaultValue;
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String value = getProperty(name);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    public Long getLongProperty(String name, Long defaultValue) {
        String value = getProperty(name);
        Long result = defaultValue;
        if (value != null) {
            try {
                result = Long.parseLong(value);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return result;
    }

    public int getIntProperty(String name, int defaultValue) {
        return getLongProperty(name, Long.valueOf(defaultValue)).intValue();
    }
}
