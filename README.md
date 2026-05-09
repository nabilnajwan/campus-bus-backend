# Campus Bus Backend

This workspace now contains the microservice version of the Campus Bus backend.

The old monolith source code has been removed from the root `src/` folder to avoid confusion. The active backend code is under `services/`.

## Active Services

| Service | Port | Database |
| --- | ---: | --- |
| `auth-service` | 8081 | `auth_db` |
| `route-service` | 8082 | `route_db` |
| `stop-service` | 8083 | `stop_db` |
| `bus-service` | 8084 | `bus_db` |
| `trip-service` | 8085 | `trip_db` |
| `location-service` | 8086 | `location_db` |

## Build Everything

```powershell
./mvnw test
```

or:

```powershell
./mvnw -f services/pom.xml test
```

## Run A Service

Example:

```powershell
./mvnw -f services/auth-service/pom.xml spring-boot:run
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

## Documentation

- Microservice guide: `services/README.md`
- Microservice code walkthrough: `docs/MICROSERVICE_CODE_WALKTHROUGH.md`
- Archived monolith learning notes: `docs/archive/MONOLITH_CODE_WALKTHROUGH.md`
