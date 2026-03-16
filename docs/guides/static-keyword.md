
# The `static` Keyword in Java

> **Meaning:** `static` members belong to the **class**, not to individual **instances**.
>
> **Implications:** Shared state or behavior across all objects of that class; accessible via `ClassName.member`.

---

## 🔹 Static Variables (Class Variables)

**What they are:** One copy per **class**, shared by all instances.

**Analogy:** Company-level attributes (e.g., `companyName`) shared by all employees.

```java
public class Employee {
    public static String companyName = "TechCorp"; // shared across all Employee instances
    private final String employeeId;                // per-instance
    private String employeeName;                    // per-instance

    public Employee(String id, String name) {
        this.employeeId = id;
        this.employeeName = name;
    }
}
```

**Enterprise usage patterns**
- **Feature flags / global toggles** (read-only after bootstrap)
- **Singleton metadata**: `VERSION`, `BUILD_TIME`, `APP_NAME`
- **Cross-cutting counters/metrics** (prefer thread-safe constructs)

> ⚠️ **Caution:** Avoid mutable `public static` fields. Prefer `private static` + accessors or immutable constants (`public static final`). For concurrent updates, use `Atomic*` or synchronized access.

**Example: shared rate-limiter budget (simplified)**
```java
public class ApiRateLimiter {
    private static final int MAX_QPS = 500;                // config constant
    private static final java.util.concurrent.atomic.AtomicInteger currentQps = new java.util.concurrent.atomic.AtomicInteger();

    public static boolean tryAcquire() {
        int qps = currentQps.incrementAndGet();
        if (qps > MAX_QPS) {
            currentQps.decrementAndGet();
            return false;
        }
        return true;
    }
}
```

---

## 🔹 Static Methods

**What they are:** Methods that belong to the **class**. They do **not** depend on instance state.

**Analogy:** A calculator function – no need to create an object to do `add(2,3)`.

```java
public class MathUtils {
    public static int add(int a, int b) {
        return a + b;
    }
}

// Usage
int sum = MathUtils.add(5, 10);
```

**When to use static methods**
- **Utility / helper** functions (pure functions, formatting, parsing)
- **Factory methods** (controlled object creation)
- **Stateless cross-cutting concerns** (e.g., hashing, encoding)
- **Performance-sensitive hot paths** avoiding virtual dispatch

**Enterprise examples**

```java
public final class ObjectMappers {
    private ObjectMappers() {}

    // Centralized, shared Jackson ObjectMapper configuration
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T fromJson(String json, Class<T> type) {
        try { return mapper.readValue(json, type); }
        catch (Exception e) { throw new RuntimeException("JSON parse error", e); }
    }

    public static String toJson(Object value) {
        try { return mapper.writeValueAsString(value); }
        catch (Exception e) { throw new RuntimeException("JSON write error", e); }
    }
}
```

```java
public final class IdFactory {
    private IdFactory() {}

    // Stateless, thread-safe ID generator (UUID-based for demo)
    public static String newOrderId() {
        return "ORD-" + java.util.UUID.randomUUID();
    }
}
```

---

## 🔹 Static Initialization Blocks

**What they are:** Blocks that run **once** when the class is **loaded**.

**Analogy:** A building’s emergency plan prepared once for everyone.

```java
public class Config {
    public static final java.util.Map<String, String> settings = new java.util.HashMap<>();

    static {
        settings.put("env", "production");
        settings.put("region", "eu-west-2");
        System.out.println("Configuration loaded!");
    }
}
```

**When to use static blocks**
- **Initialize complex static fields** that require logic
- **One-time bootstrap** of caches, registries, codecs
- **Register SPI implementations** (be mindful of classloading order)

> ⚠️ **Caution:** Work done here impacts **class-loading time**. Avoid I/O or network calls in static blocks in latency-sensitive services. Prefer lazy initialization via `Holder` pattern when feasible.

**Lazy holder pattern (recommended)**
```java
public final class Secrets {
    private Secrets() {}

    private static class Holder {
        // Loaded on first access to get(), not on class load
        static final java.util.Map<String, char[]> secrets = load();
        private static java.util.Map<String, char[]> load() {
            java.util.Map<String, char[]> m = new java.util.HashMap<>();
            m.put("db.password", "s3cr3t".toCharArray());
            return java.util.Collections.unmodifiableMap(m);
        }
    }

    public static char[] get(String key) { return Holder.secrets.getOrDefault(key, new char[0]); }
}
```

---