package urlShortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EndpointLogger implements ApplicationListener<ApplicationReadyEvent> {
    private Logger logger = Logger.getLogger(EndpointLogger.class.getName());
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        handlerMapping.getHandlerMethods().forEach((key, value) -> {
            logger.log(Level.INFO, ">> Mapped: " + key + " -> " + value);
        });
    }
}
