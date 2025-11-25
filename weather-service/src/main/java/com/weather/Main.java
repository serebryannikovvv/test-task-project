package com.weather;

import com.weather.config.Config;
import com.weather.server.WeatherServlet;
import com.weather.service.WeatherService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        WeatherService service = new WeatherService();

        Server server = new Server(Config.serverPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new WeatherServlet(service)), "/weather");

        log.info("Сервер запущен → http://localhost:8080/weather?city=Moscow");
        server.start();
        server.join();
    }
}