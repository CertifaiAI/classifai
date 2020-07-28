package ai.classifai.config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

@Slf4j
@NoArgsConstructor
public class LogFileConfig
{
    private String propertyFileName = "config.properties";
    private Properties prop = null;

    public void initiate()
    {
        prop = new Properties();

        URL url = LogFileConfig.class.getResource(propertyFileName);

        try
        {
            if(url != null)
            {
                prop.load(url.openStream());
            }
        }
        catch(Exception e)
        {
            log.debug("Properties file could not be open for writing. ", e);
        }

        this.saveConfig();

        try
        {
            String outputConfig = this.getClass().getResource("/").getPath() + propertyFileName;

            System.out.println(outputConfig);
            log.info("Properties Path: " + outputConfig);

            prop.store(new FileOutputStream(outputConfig), null);

            log.debug("Save to " + outputConfig);
        }
        catch(Exception e)
        {
            log.debug("Properies file could not been saved. ", e);
        }

    }

    private void saveConfig()
    {
        prop.setProperty("LOG_PATH", "/Users/wei/Desktop/temp");
    }
}
