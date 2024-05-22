package com.caixy.adminSystem.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地保存文件配置
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.LocalFileConfig
 * @since 2024-05-21 20:15
 **/
@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Slf4j
public class LocalFileConfig
{
    private String location;
    @Getter
    private Path rootLocation;
    @Setter
    private Boolean forceCreated;
    @Setter
    private String staticPath;

    public void setLocation(String location)
    {
        this.location = location;
        // （如果存在）去除file:前缀
        if (location.startsWith("file:"))
        {
            location = location.substring(5);
        }
        this.rootLocation = Paths.get(location).toAbsolutePath();
    }

    @PostConstruct
    public void init()
    {
        try
        {
            File directory = rootLocation.toFile();
            if (!directory.exists())
            {
                boolean initDir = directory.mkdirs();
                if (!initDir)
                {
                    log.error("Could not initialize storage location: {}", directory.getAbsolutePath());
                    if (forceCreated != null && forceCreated)
                    {
                        throw new IllegalArgumentException("Force created storage location: " + directory.getAbsolutePath());
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Could not initialize storage location: {}", rootLocation, e);
            if (forceCreated != null && forceCreated)
            {
                throw new IllegalArgumentException("Could not initialize storage location", e);
            }
        }
    }
}
