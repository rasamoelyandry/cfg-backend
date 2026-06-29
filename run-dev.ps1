# Pointe Maven sur JDK 21 (Lombok 1.18.32 ne supporte pas JDK 22+)
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.5.11-hotspot"

# Variables DB / Redis (ajuste si nécessaire)
$env:DB_URL      = "jdbc:postgresql://localhost:5432/cfg"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "andry"
$env:REDIS_HOST  = "localhost"
$env:REDIS_PORT  = "6379"
$env:REDIS_PASSWORD = ""
$env:JWT_SECRET  = "cfg-dev-secret-must-be-at-least-256-bits-long-for-hs256-algo"

Write-Host "JAVA_HOME -> $env:JAVA_HOME" -ForegroundColor Cyan
mvn spring-boot:run
