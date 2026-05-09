package com.mjc.school.service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="newsdata.api")
public class NewDataApiProperties {

    private String key;
    public String getKey(){return key;}
    public void setKey(String key){this.key = key;}

}
