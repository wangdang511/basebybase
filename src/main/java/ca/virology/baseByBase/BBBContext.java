package ca.virology.baseByBase;

import ca.virology.baseByBase.config.spring.BBBConfig;
import ca.virology.lib2.common.service.MessageService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BBBContext {
    MessageService messageService;

    public BBBContext() {
        //This is required for the jnlp.properties file to be loaded if BaseByBase is executed by another jar (For example, vocs)
        ClassLoader loader = Runtime.getRuntime().getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            System.out.println("Initializing application context");
            ApplicationContext context = new AnnotationConfigApplicationContext(BBBConfig.class);
            messageService = context.getBean(MessageService.class);
        } catch (Exception e) {
            System.out.println("Failed to initialize application context: " + e.toString());
            //e.printStackTrace();
            //System.exit(1);
        }
    }

    public MessageService getMessageService() {
        return messageService;
    }
}
