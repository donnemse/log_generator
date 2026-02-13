# LogGenerator

A web application that generates realistic log data in real-time using YAML-based configurations. Suitable for testing, benchmarking, and simulation environments where synthetic log data is needed.

## Features

- **YAML-based Log Templates** - Define log formats, field types, and value distributions in YAML
- **Multiple Field Types** - Supports IP, string, integer, timestamp, URL, payload, and more
- **Probability-based Value Distribution** - Assign weights to field values for realistic log generation
- **Multiple Outputs** - File, TCP socket server (Netty), and other output destinations
- **Real-time EPS Monitoring** - Visualize events per second with interactive charts
- **Logger Management UI** - Create, edit, start/stop loggers from the web browser
- **History Management** - Track configuration changes with diff comparison
- **IP Geolocation** - Map IP addresses to country information

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 8, Spring Boot 2.6, MyBatis, SQLite |
| Frontend | Mustache, Bootstrap, jQuery, Ace Editor, Highcharts |
| Network | Netty (TCP socket server) |
| Build | Maven, Gradle |
| Container | Docker |

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven or Gradle

### Build & Run

**Maven:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Gradle:**

```bash
./gradlew clean build
./gradlew bootRun
```

**Docker:**

```bash
# Build with Gradle first
./gradlew clean build

docker build --tag log-generator:1.0 ./
docker run -d -it -p 8080:8080 log-generator:1.0
```

Open `http://localhost:8080` in your browser after starting the application.

## YAML Configuration Example

Apache log generation configuration:

```yaml
log: apache
eps: 60
logtype: WEB
raw: |
   ${sip} - - [${event_time}] "${method} ${url} ${http_version}" ${status} ${event_size}
data:
   sip:
      type: ip
      values:
         "192.168.1.1/24": 0.1
         "192.168.1.100": 0.1
   event_time:
      type: time
      raw_format: dd/MMM/yyyy:HH:mm:ss Z
      parse_format: yyyyMMddHHmmssSSS
   method:
      type: str
      values:
         GET: 0.4
         POST: 0.4
         UPDATE: 0.1
         DELETE: 0.1
   status:
      type: int
      values:
         10-100: 0.5
         500: 0.3
         400: 0.2
```

### Field Types

| Type | Description | Example |
|------|-------------|---------|
| `ip` | IP address (CIDR supported) | `"192.168.1.0/24": 0.5` |
| `str` | String | `"GET": 0.4` |
| `int` | Integer (range supported) | `10-100: 0.5` |
| `time` | Timestamp | `raw_format: yyyy-MM-dd HH:mm:ss` |
| `url` | URL path | `"/api/v1/users": 0.3` |
| `payload` | Payload data | `"raw data...": 0.1` |

## Project Structure

```
src/main/java/com/yuganji/generator/
├── configuration/     # Spring configs (Swagger, AOP, Async)
├── controller/        # REST API controllers
├── db/                # Database entities & repositories
├── engine/            # Log generation engine
├── output/            # Output handlers (file, TCP)
├── logger/            # Logger service & caching
├── queue/             # Queue management
├── monitor/           # EPS monitoring
├── history/           # History tracking
├── field/             # Field value generation
├── model/             # DTOs & data models
└── util/              # Utilities
```

## API

API documentation is available via Swagger UI: `http://localhost:8080/swagger-ui/`
