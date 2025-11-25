# Weather Forecast Service

**Тестовое задание: Сервис прогноза погоды с кэшированием в Redis и визуализацией температуры**

Реализован **на Java 8**,  с использованием embedded Jetty, Redis и JFreeChart.

**Особенности:**
- Полностью конфигурируемый через `application.properties`
- Кэширование данных в Redis с TTL = 15 минут (по умолчанию)
- Graceful degradation — работает даже без Redis
- Логирование (SLF4J + Logback)
- В проекте приложена коллекция для Postman:
  → postman/weather_service_test.postman_collection.json

**API:**
GET /weather?city=Moscow
GET /weather?city=Saint Petersburg
GET /weather?city=Voronez

### Как запустить
#### (Сервис работает и без Redis тогда запросы берутся на прямую с API)

```bash
# 1. Запустить Redis
docker run -d --name redis-weather -p 6379:6379 redis:7-alpine

# 2. Собрать и запустить сервис
./gradlew run