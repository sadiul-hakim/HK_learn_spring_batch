package xyz.sadiulhakim.advanced_project.listener;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppListener {

    @EventListener
    void serverStarted(WebServerInitializedEvent event) {
        System.out.println("App is running on port : " + event.getWebServer().getPort());
    }
}
