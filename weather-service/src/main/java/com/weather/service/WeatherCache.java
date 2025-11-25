// src/main/java/com/weather/service/WeatherCache.java
package com.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.config.Config;
import com.weather.config.JsonMapper;
import com.weather.model.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class WeatherCache {
    private static final Logger log = LoggerFactory.getLogger(WeatherCache.class);

    private final JedisPool pool;
    private final ObjectMapper mapper = JsonMapper.get();
    private static final int TTL = Config.redisTtlSeconds();
    private final boolean redisAvailable;

    public WeatherCache(String host, int port) {
        JedisPool tempPool = null;
        boolean available = false;
        try {
            tempPool = new JedisPool(host, port);
            try (Jedis jedis = tempPool.getResource()) {
                jedis.ping();
                available = true;
                log.info("Redis успешно подключён: {}:{}", host, port);
            }
        } catch (Exception e) {
            log.warn("Redis недоступен ({}:{}) — кэширование отключено.", host, port);
            log.debug("Redis недоступен: {}", e.toString());
        }
        this.pool = tempPool;
        this.redisAvailable = available;
    }

    public void put(String city, WeatherResponse response) {
        if (!redisAvailable || pool == null) {
            log.debug("Кэширование отключено : {}", city);
            return;
        }

        try (Jedis jedis = pool.getResource()) {
            String json = mapper.writeValueAsString(response);
            jedis.setex("weather:" + city.toLowerCase(), TTL, json);
            log.info("Данные для города '{}' закэшированы в Redis на 15 минут", city);
        } catch (Exception e) {
            log.warn("Не удалось записать в Redis город: {}", city, e);
        }
    }

    public WeatherResponse get(String city) {
        if (!redisAvailable || pool == null) {
            log.debug("Кэширование отключено — прямой запрос к API для города: {}", city);
            return null;
        }

        try (Jedis jedis = pool.getResource()) {
            String json = jedis.get("weather:" + city.toLowerCase());
            if (json != null) {
                return mapper.readValue(json, WeatherResponse.class);
            } else {
                return null;
            }
        } catch (JedisConnectionException e) {
            log.warn("Ошибка Redis во время чтения — переключаемся на прямые запросы", e);
            return null;
        } catch (Exception e) {
            log.error("Ошибка десериализации из Redis для города: {}", city, e);
            return null;
        }
    }
}