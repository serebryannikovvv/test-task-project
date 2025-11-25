// src/main/java/com/weather/chart/TemperatureChartGenerator.java
package com.weather.chart;

import com.weather.model.HourlyTemperature;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

public class TemperatureChartGenerator {

    public static byte[] generatePng(Iterable<HourlyTemperature> data, String city) throws Exception {
        TimeSeries series = new TimeSeries("Температура");
        for (HourlyTemperature h : data) {
            LocalDateTime ldt = h.getTime();
            Hour hour = new Hour(ldt.getHour(), ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
            series.add(hour, h.getTemperature());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Прогноз температуры в " + city + " — следующие 24 часа",
                "Время",
                "Температура, °C",
                dataset,
                false, true, false
        );

        // === СТИЛИЗАЦИЯ ===
        chart.setBackgroundPaint(new Color(250, 251, 253));
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 24));
        chart.getTitle().setPaint(new Color(50, 60, 80));

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);

        // Ось X — время
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);

        // Ось Y — температура
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));
        rangeAxis.setAutoRangeIncludesZero(false);

        // Рендерер линии
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, new Color(102, 126, 234));           // линия
        renderer.setSeriesStroke(0, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-6, -6, 12, 12));
        renderer.setSeriesPaint(0, new Color(102, 126, 234));
        renderer.setSeriesFillPaint(0, new Color(102, 126, 234, 30));

        // Заливка под линией (градиент)
        GradientPaint gradient = new GradientPaint(0, 0, new Color(102, 126, 234, 80),
                0, 600, new Color(118, 75, 162, 0));
        renderer.setSeriesPaint(0, gradient);
        renderer.setUseFillPaint(true);
        renderer.setSeriesFillPaint(0, gradient);

        plot.setRenderer(renderer);

        // Антиалиасинг и качество
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        org.jfree.chart.ChartUtils.writeChartAsPNG(out, chart, 1200, 600, true, 9);
        return out.toByteArray();
    }
}