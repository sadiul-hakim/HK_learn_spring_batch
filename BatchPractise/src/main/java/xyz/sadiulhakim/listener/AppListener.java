package xyz.sadiulhakim.listener;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppListener {

    @EventListener
    void severStarted(WebServerInitializedEvent event) {
        System.out.println("Server is running on port : " + event.getWebServer().getPort());
    }
}
