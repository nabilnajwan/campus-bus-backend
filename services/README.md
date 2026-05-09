# Campus Bus Microservices

This folder contains the active microservice version of the Campus Bus backend.

The old monolith source code has been removed from the root `src/` folder to avoid confusion. The archived monolith learning notes are under `docs/archive/`.

## Services

| Service | Port | Database | Responsibility |
| --- | ---: | --- | --- |
| `auth-service` | 8081 | `auth_db` | Login, users, JWT issuing |
| `route-service` | 8082 | `route_db` | Routes |
| `stop-service` | 8083 | `stop_db` | Stops by route id |
| `bus-service` | 8084 | `bus_db` | Buses by route id |
| `trip-service` | 8085 | `trip_db` | Driver starts trips |
| `location-service` | 8086 | `location_db` | GPS updates and live bus lookup |

## Important Microservice Difference

In the monolith, JPA entities can reference each other directly, for example `Bus -> Route`.

In microservices, each service owns its own database. Because of that, cross-service relationships are stored as IDs only:

- `stop-service` stores `routeId`, not a `Route` entity.
- `bus-service` stores `routeId`, not a `Route` entity.
- `trip-service` stores `busId` and `driverId`, not `Bus` and `User` entities.
- `location-service` stores `tripId`, `busId`, and `driverId`, not `Trip` and `Bus` entities.

This is the correct first step for separating databases.

For a more detailed learning walkthrough, read `docs/MICROSERVICE_CODE_WALKTHROUGH.md` from the project root.

## JWT Responsibility

- `auth-service` issues JWT tokens.
- Other services validate JWT tokens using the same shared secret for now.
- Later, a better production design would use asymmetric keys: auth-service signs with a private key, other services verify with a public key.

## Create Databases In Docker PostgreSQL

Run these commands once against your existing `postgres-db` container:

```powershell
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE auth_db;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE route_db;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE stop_db;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE bus_db;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE trip_db;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE location_db;"
```

If a database already exists, PostgreSQL will show an error for that one. That is fine.

## Build All Services

From the project root:

```powershell
mvn -f services/pom.xml test
```

## Run One Service

Example:

```powershell
mvn -f services/auth-service/pom.xml spring-boot:run
```

Swagger URLs:

- Auth: http://localhost:8081/swagger-ui.html
- Route: http://localhost:8082/swagger-ui.html
- Stop: http://localhost:8083/swagger-ui.html
- Bus: http://localhost:8084/swagger-ui.html
- Trip: http://localhost:8085/swagger-ui.html
- Location: http://localhost:8086/swagger-ui.html

## Current Limitations

This split is a first microservice scaffold, not a full production system yet.

Still missing for a complete production-grade microservice architecture:

- API gateway
- service discovery
- inter-service HTTP clients
- centralized config
- per-service Dockerfiles
- docker-compose for all services
- distributed transaction strategy
- route/bus/trip enrichment across services

For the assignment and learning stage, this is a safe first split.
