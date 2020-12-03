package org.openmrs.module.appointments.service.impl;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component("emailTemplateConfig")
public class EmailTemplateConfig {


    private Properties properties;

    private void loadProperties() throws IOException {
        String resourceName = "email-template.properties";
        final File file = new File(OpenmrsUtil.getApplicationDataDirectory(), resourceName);
        final InputStream inputStream;
        if (file.exists()) {
            inputStream = new FileInputStream(file);
        } else {
            inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (inputStream == null) {
                properties = null;
                return;
            }
        }
        properties.load(inputStream);
    }

    public Properties getProperties() throws IOException {
        if (properties == null) {
            properties = new Properties();
            loadProperties();
        }
        return properties;
    }
}
