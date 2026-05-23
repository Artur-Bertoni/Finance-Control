package com.financecontrol.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = findEnvFile();
        if (envFile == null) return;

        Properties props = new Properties();
        try (FileReader reader = new FileReader(envFile)) {
            props.load(reader);
        } catch (Exception e) {
            return;
        }

        // addLast = lowest priority; OS env vars and system properties always win
        environment.getPropertySources().addLast(new PropertiesPropertySource(".env", props));
    }

    private File findEnvFile() {
        File dir = new File(".").getAbsoluteFile().getParentFile();
        for (int i = 0; i < 4 && dir != null; i++) {
            File candidate = new File(dir, ".env");
            if (candidate.isFile()) return candidate;
            dir = dir.getParentFile();
        }
        return null;
    }
}
