package ca.virology.baseByBase.config.spring;

import ca.virology.lib2.common.config.spring.ServiceConfig;
import ca.virology.lib2.db.config.spring.DatabaseConnectionConfig;
import ca.virology.lib2.db.config.spring.ModelDaoConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource({"classpath:config/jdbc.properties", "${appclient}", "${externalapps}"})
@Import({ModelDaoConfig.class, DatabaseConnectionConfig.class, ServiceConfig.class})
public class BBBConfig {

}

