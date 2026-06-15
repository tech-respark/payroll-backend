package com.relfor.pcs.payroll.config;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class VelocityConfig {

    @Bean
    public VelocityEngine velocityEngine() {
        Properties properties = new Properties();

        // 1. Resource Loader (Velocity 2.3 syntax)
        properties.setProperty("resource.loaders", "class");
        properties.setProperty("resource.loader.class.class", 
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        // 2. Migration & Compatibility Settings
        // Allows hyphens in variable names ($my-var)
        properties.setProperty("parser.allow_hyphen_in_identifiers", "true");
        
        // Backward compatibility for space/newline handling
        properties.setProperty("parser.space_gobbling", "bc");
        
        // Suppress logs for invalid references to keep console clean
        properties.setProperty("runtime.log.log_invalid_references", "false");
        
        // Enable strict mode for better debugging and data integrity
        //properties.setProperty("runtime.references.strict", "true");

        // 3. Optional: Character Encoding
        properties.setProperty("input.encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");

        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperties(properties);
        velocity.init();
        return velocity;
    }
}