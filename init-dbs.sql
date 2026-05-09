docker-compose up -d-- Initialize databases for campus-bus microservices
CREATE DATABASE auth_db;
CREATE DATABASE bus_db;
CREATE DATABASE location_db;
CREATE DATABASE route_db;
CREATE DATABASE stop_db;
CREATE DATABASE trip_db;

-- Grant privileges to admin user (optional, but good practice)
GRANT ALL PRIVILEGES ON DATABASE auth_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE bus_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE location_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE route_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE stop_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE trip_db TO admin;
