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

### Part 1
**1. What is the default lifecycle of JAX-RS resource classes? How would you maintain state temporarily across multiple requests without using a database?**
By default, JAX-RS Resource classes are per-request, meaning a completely new instance is instantiated for every incoming HTTP request and instantly destroyed afterwards. Because of this, instance variables inside the `RoomResource.java` class wipe out constantly. To maintain state without a database, we must delegate the data storage to an external Singleton design pattern (like the `DataStore` class) or use `static` variable environments backed by thread-safe classes like `ConcurrentHashMap` to ensure the data persists across requests.

**2. What is the concept of HATEOAS, and how does returning hypermedia links benefit a client integrating with your API?**
HATEOAS stands for Hypermedia as the Engine of Application State. When a client consumes the API, sending dynamically generated URL links nested inside the JSON responses acts as an active navigator. If the `/rooms` endpoint ever changes in the future, the client application can simply follow the new links dynamically instead of having hard-coded legacy routes break their application, providing superior loosely-coupled design.

### Part 2
**3. What are the advantages and disadvantages of returning only resource IDs versus returning full nested object representations?**
Returning only IDs (e.g., returning `"roomId": "ROOM-A2"`) significantly reduces bandwidth usage and response latency by shrinking the JSON payload sizes, but it is disadvantageous because it forces the client to dispatch secondary API calls to resolve nested relationships (known as the `N+1` lookup burden). Returning full nested objects optimizes the client’s logic by serving everything simultaneously, but consumes far more server memory per request.

**4. Is the DELETE operation implemented for removing a room idempotent? Explain what idempotency means in REST and how your implementation adheres to this principle.**
Yes, this implementation is idempotent. In REST, idempotency means that making multiple identical requests has the exact same effect on the server as making a single request. On the first `DELETE /rooms/{id}` call, the API removes the Room resource (returning `204 No Content`). If a client accidentally sends duplicate `DELETE` requests with the exact same ID afterwards, the system identifies the Room is already missing and safely returns a `404 Not Found` without causing any cascading recursive damage to the data layer.

### Part 3
**5. What happens if a client submits an incorrectly formatted payload, like text instead of JSON, to an endpoint expecting @Consumes? How does JAX-RS handle it by default?**
If a client drops unstructured `text/plain` or XML payloads to an endpoint that is rigidly bounded by the `@Consumes(MediaType.APPLICATION_JSON)` annotation, the application infrastructure prevents the request from ever hitting the logic tier. JAX-RS automatically intercepts the payload mismatch and returns a `415 Unsupported Media Type` HTTP error to the client natively. 

**6. Why use query parameters (`?type=value`) instead of path parameters for filtering or searching collections like sensors?**
`Path` parameters are hierarchical and definitively pinpoint static distinct identifiers (e.g. locating a specific entity at `/sensors/123`). Conversely, `Query` parameters designate metadata filters to loosely dissect broader collections (e.g. `/sensors?type=Temperature`). Using Query parameters for searching is infinitely scalable and chain-able (e.g., `?type=CO2&status=ACTIVE`), making filtering vastly superior without structurally polluting or complicating the strict semantic URL paths.

### Part 4
**7. Explain the Sub-Resource Locator approach and why it is beneficial for managing related entity models.**
The Sub-Resource Locator pattern (e.g., returning a new Java Resource class instead of an HTTP response directly on `@Path("/{sensorId}/readings")`) effectively segregates monolithic API controllers. Instead of bloating `SensorResource.java` with thousands of unreadable lines of reading logic, it elegantly delegates execution to a standalone `SensorReadingResource.java` file. This drastically increases codebase maintainability, separation of concerns, and keeps domain logic sharply organized.

### Part 5
**8. In Part 5, you map dependency validation failures (e.g., missing Room ID) to HTTP 422. Why is HTTP 422 more appropriate here than a generic HTTP 404 (Not Found)?**
A generic HTTP `404 Not Found` strictly implies the target URL endpoint itself does not exist. However, when posting to a completely valid endpoint (`POST /sensors`), but an internal foreign-key reference within that JSON is missing (an invalid `roomId`), the JSON syntax is perfectly clean but the semantic business instructions fail. This makes returning HTTP `422 Unprocessable Entity` technically and stylistically ideal for dependency violations.

**9. Why is it important to prevent raw stack traces from being returned to the client in HTTP 500 errors?**
Leaking Java stack traces grants hostile attackers free topological mapping of internal class paths, libraries, and logic frameworks. A Global `ExceptionMapper` intercepts crashes and responds with a unified, clean JSON error, preventing precision adversarial reconnaissance and shielding internal structural vulnerabilities from the public.

**10. Why implement a Logging Filter instead of directly putting logging statements in every resource method?**
Directly polluting `Logger.info()` inside every single controller violates DRY (Don't Repeat Yourself) principles and clutters business logic. JAX-RS standard Container Filters enable traffic observance transversally across the whole API centrally, completely decoupling the cross-cutting diagnostic logging capabilities from specific business domain logic.

