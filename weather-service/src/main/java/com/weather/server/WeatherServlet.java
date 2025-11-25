package com.weather.server;

import com.weather.chart.TemperatureChartGenerator;
import com.weather.model.HourlyTemperature;
import com.weather.model.WeatherResponse;
import com.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

public class WeatherServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(WeatherServlet.class);
    private final WeatherService service;

    public WeatherServlet(WeatherService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String city = req.getParameter("city");
        if (city == null || city.trim().isEmpty()) {
            resp.sendError(400, "Параметр city обязателен");
            return;
        }

        try {
            WeatherResponse weather = service.getWeather(city);
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write(buildHtml(weather));
        } catch (IllegalArgumentException e) {
            resp.sendError(404, e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке запроса city={}", city, e);
            resp.sendError(500, "Внутренняя ошибка сервера");
        }
    }

    private String buildHtml(WeatherResponse weatherResp) throws Exception {
        byte[] png = TemperatureChartGenerator.generatePng(weatherResp.getHourly(), weatherResp.getCity());
        String base64 = Base64.getEncoder().encodeToString(png);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
                .append("<title>Погода в ").append(weatherResp.getCity()).append("</title>")
                .append("<style>body{font-family:Arial;text-align:center;margin:40px;}</style></head><body>")
                .append("<h1>Погода: ").append(weatherResp.getCity()).append("</h1>")
                .append("<img src='data:image/png;base64,").append(base64).append("'/><br><br>")
                .append("<table border='1' style='margin:auto;'><tr><th>Время</th><th>Температура °C</th></tr>");

        for (HourlyTemperature temperature : weatherResp.getHourly()) {
            sb.append("<tr><td>").append(temperature.getTime().toLocalTime())
                    .append("</td><td>").append(String.format("%.1f", temperature.getTemperature()))
                    .append("</td></tr>");
        }
        sb.append("</table><p><small>Кэш: 15 минут</small></p></body></html>");
        return sb.toString();
    }
}