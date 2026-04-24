# Smart Campus Sensor & Room Management API

## 1. API Design & Architecture Overview
This **Smart Campus API** provides a powerful RESTful interface to structurally manage Rooms and Sensors deployed across the campus environment. It relies exclusively on **Jakarta EE (JAX-RS)** and implements state using thread-safe, in-memory architectures (e.g., `ConcurrentHashMap`), perfectly satisfying the coursework constraints.

- **Rooms** (`/api/v1/rooms`): Logically managed physical entities.
- **Sensors** (`/api/v1/sensors`): Dynamically registered IoT units linked to specific rooms.
- **Sensor Readings** (`/api/v1/sensors/{sensorId}/readings`): Isolated measurements mapped using the Sub-Resource Locator architectural pattern.
- **Data Integrity**: Handled dynamically by JAX-RS `ExceptionMapper`s intercepting unsafe deletions and validation failures.

---

## 2. Build & Launch Instructions

The application has been explicitly engineered for embedded execution to eliminate the need for heavy application server installations.

### Launch via NetBeans IDE (Recommended)
1. Open NetBeans and go to **File** -> **Open Project**. Select the `CSA_coursework` directory.
2. In the **Projects** tab, open **Source Packages** -> `com.mycompany.csa_coursework`.
3. Right-click the **`Main.java`** file and select **Run File**.
4. The embedded Grizzly HTTP server will boot instantly in the terminal.

### Launch via Command-Line
Navigate to the `CSA_coursework` directory and run:
```bash
mvn clean package exec:java
```
**API Base URL:** `http://localhost:8080/api/v1/`

---

## 3. Postman / cURL Testing Commands
*(These commands use static IDs for clean, easily reproducible testing)*

**1. Discovery Endpoint**
```bash
curl -X GET http://localhost:8080/api/v1/
```

**2. Create a Room (Custom ID: ROOM-A2)**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
 -H "Content-Type: application/json" \
 -d "{\"id\":\"ROOM-A2\",\"name\":\"Quiet Study\",\"capacity\":50}"
```

**3. Register a Sensor to the Room (Custom ID: S-001)**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
 -H "Content-Type: application/json" \
 -d "{\"id\":\"S-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"roomId\":\"ROOM-A2\"}"
```

**4. Filter Sensors by Type**
```bash
curl -X GET 'http://localhost:8080/api/v1/sensors?type=Temperature'
```

**5. Post a new Sensor Reading**
```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-001/readings \
 -H "Content-Type: application/json" \
 -d "{\"id\":\"RD-001\",\"value\":23.5}"
```

---

## 4. Coursework Question Responses

### 4.1 System Overview Strategy
**Benefits of Client-Server Architecture for Smart Campus:**
The Client-Server architecture isolates the central computation engine (Campus API) from edge hardware units (IoT Sensors). This allows thousands of decentralized temperature and CO2 sensors (clients) to securely submit lightweight telemetry data without bearing processing loads. It provides simplified vertical scaling, single-point authorization, and ensures data is synchronized universally across campus dashboards.

**Why JAX-RS is suitable for the API:**
JAX-RS provides native, standard-compliant RESTful networking without enforcing massive unneeded framework bloat. It leverages expressive annotations (like `@GET` and `@Consumes`) that seamlessly map HTTP concepts directly to Java logic, making it academically and practically ideal for lightweight API implementation.

### 4.2 Endpoint Design Strategies
**The Purpose of `@Path` Multiple Times:**
Using `@Path` multiple times allows for hierarchical URI modeling. A class-level `@Path("/sensors")` defines the root domain resource, while method-level annotations like `@Path("/{sensorId}")` define specific nested sub-entities. This enforces clean, semantic URL structures without duplicating string paths across the controller.

**Sub-Resource Locator Approach:**
The Sub-Resource Locator pattern (returning a new Resource class instead of an HTTP response on `@Path("/{sensorId}/readings")`) segregates monolithic API controllers. Instead of bloating `SensorResource.java` with reading logic, it elegantly delegates execution to `SensorReadingResource.java`, increasing codebase maintainability and keeping domain execution sharply organized.

**Query Parameters vs Path Parameters:**
`Path` parameters identify specific objects (e.g., retrieving sensor `S-001`). `Query` parameters filter or modify collections (e.g., `?type=Temperature`). Using Query parameters for searching is infinitely scalable (`?type=CO2&status=ACTIVE`) and protects the strict URI structure from becoming convoluted.

### 4.3 Error Handling & Data Integrity
**Idempotency of `DELETE`:**
This implementation's `DELETE /rooms/{id}` method is idempotent. Modifying or destroying a resource should behave reliably; if a room is deleted, it returns `204 No Content`. If the exact same request triggers again, it safely drops to a `404 Not Found` without causing cascading damage to the database.

**Appropriateness of 422 vs 404:**
When a user attempts to bind a Sensor to a `roomId` that doesn't exist, throwing a `422 Unprocessable Entity` is standard. A `404 Not Found` strictly implies the *URL endpoint* doesn't exist. The `422` highlights that the JSON structure is completely valid, but the internal semantic foreign key violates business logic.

**Risks of Showing Stack Traces:**
Leaking Java stack traces via HTTP `500` errors grants hostile attackers free topological mapping of internal class paths, libraries, and logic frameworks. A Global `ExceptionMapper` intercepts these, responding with a unified, clean JSON error, preventing precision adversarial reconnaissance.

