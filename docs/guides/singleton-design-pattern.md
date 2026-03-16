
# Singleton Design Pattern

> **Intent**: Ensure a **class has only one instance** and provide a **global access point** to it.
>
> **Core idea**: Centralize shared, process-wide state/behavior that must be singular (e.g., configuration registry, token cache, scheduler, key vault client), while preserving thread-safety, testability, and evolvability.

---

## 🧠 When to Use a Singleton

Choose a singleton **only if** all of the following apply:

- There must be **exactly one** logical instance per JVM/process (e.g., one OAuth token manager, one metrics sink, one scheduler).
- You need **easy global access** without passing references everywhere.
- The object is **expensive to create**, and reusing it improves performance or resource usage.
- The instance is **stateless** or **carefully synchronized** (if it has mutable state).

**Prefer alternatives** if:
- You’re inside a DI framework (Spring/Micronaut) that already provides **singleton-scoped beans**. Use DI instead of manual singletons.
- You’re tempted to use singletons as **global state**. It complicates testing and coupling.
- You need **multiple configurations** of the same type (e.g., multi-tenant clients). Consider a registry/factory instead.

---

## 🏗️ Implementations (Java 17/21)

### 1) **Enum Singleton** (Recommended)

- **Thread-safe**, **serialization-safe**, and **resistant to reflection** by design.
- Simple and robust for most use cases.

```java
public enum ConfigRegistry {
    INSTANCE;

    private final java.util.Properties props = new java.util.Properties();

    ConfigRegistry() {
        // light init; avoid long I/O here
        props.setProperty("env", System.getProperty("app.env", "prod"));
    }

    public String get(String key) { return props.getProperty(key); }
}

// Usage
String env = ConfigRegistry.INSTANCE.get("env");
```

### 2) **Initialization-on-Demand Holder** (Lazy, Thread-safe)

- Lazy without synchronization overhead.
- Safe under the Java Memory Model.

```java
public final class ServiceRegistry {
    private ServiceRegistry() {}

    private static class Holder { static final ServiceRegistry INSTANCE = new ServiceRegistry(); }

    public static ServiceRegistry getInstance() { return Holder.INSTANCE; }
}
```

### 3) **Double-Checked Locking** (Lazy + `volatile`)

- Use when you must support lazy init **and** you cannot use the holder idiom.

```java
public final class AuditSink {
    private static volatile AuditSink instance;

    private AuditSink() {}

    public static AuditSink getInstance() {
        AuditSink local = instance;
        if (local == null) { // first check (no lock)
            synchronized (AuditSink.class) {
                local = instance;
                if (local == null) {
                    local = instance = new AuditSink();
                }
            }
        }
        return local;
    }
}
```

### 4) **Eager Initialization** (Simple but not lazy)

```java
public final class Metrics { 
    private static final Metrics INSTANCE = new Metrics();
    private Metrics() {}
    public static Metrics getInstance() { return INSTANCE; }
}
```

> ✅ **Rule of thumb**: Prefer **Enum** for simplicity/safety, or **Holder** for lazy init. Avoid hand-rolled synchronization unless necessary.

---

## 🧪 Serialization & Reflection Safety

### Problem: Serialization can create **new instances**

```java
public final class SingletonWithSerialization implements java.io.Serializable {
    private static final SingletonWithSerialization INSTANCE = new SingletonWithSerialization();
    private SingletonWithSerialization() {}
    public static SingletonWithSerialization getInstance() { return INSTANCE; }

    // ensure the singleton after deserialization
    private Object readResolve() throws java.io.ObjectStreamException { return INSTANCE; }
}
```

### Problem: Reflection can break private constructor

```java
public final class ReflectionSafeSingleton {
    private static volatile boolean created = false;
    private static final ReflectionSafeSingleton INSTANCE = new ReflectionSafeSingleton();

    private ReflectionSafeSingleton() {
        if (created) throw new IllegalStateException("Use getInstance()");
        created = true;
    }

    public static ReflectionSafeSingleton getInstance() { return INSTANCE; }
}
```

> ✅ **Enum singletons** are inherently safe against both serialization and reflection tricks (preferred where feasible).

---

## 🧰 DI vs Manual Singletons

- In **Spring** (default scope is singleton), prefer:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public TokenProvider tokenProvider() {
        return TokenProvider.getInstance(); // or better: a non-static class managed by Spring
    }
}
```

- Better yet, refactor `TokenProvider` into a **normal class** and let the container enforce singletons:

```java
import org.springframework.stereotype.Component;

@Component // default singleton scope in Spring
public class TokenProviderBean { /* same logic as above, but no static */ }
```

**Why DI**: Improves **testability**, supports **profiles/config**, handles **lifecycle** and **observability** hooks. Manual singletons are fine for low-level utilities, but DI is generally cleaner for services.

---

## 🧪 Testing Strategies

- Expose **package-private setters/constructors** (or use DI) for injecting test doubles.
- Provide a **reset** method only in test scope (e.g., via `@VisibleForTesting`) if necessary.
- Avoid global hidden state; document any mutable singletons and design idempotent operations.

```java
class TokenProviderTest {
    @org.junit.jupiter.api.Test
    void returnsCachedTokenWhenFresh() {
        var clock = new Clock() { public java.time.Instant now() { return java.time.Instant.parse("2030-01-01T00:00:00Z"); } };
        var tp = new TokenProviderTestHarness(clock); // test-only helper to bypass static
        // ... assert behavior
    }
}
```

---

## ⚠️ Pitfalls & Gotchas

- **Hidden global state** → tight coupling and flaky tests.
- **Multiple classloaders** (app servers, plugins) can yield **multiple instances**.
- **Serialization without `readResolve`** breaks the singleton guarantee.
- **Complex logic in static initializers** slows classloading, can cause deadlocks if not careful.
- **Concurrency bugs**: missing `volatile`, unnecessary synchronization causing contention.

---

## ✅ Checklist

- Do you **truly need one** instance? Consider DI scope first.
- Can you use **`enum`**? Prefer it.
- Need **lazy init**? Use **holder idiom**.
- Will you serialize the singleton? Provide **`readResolve`** (unless enum).
- Could reflection instantiate it? Guard constructor (or use enum).
- Have you considered **testing**, **classloader** behavior, **observability**?

---
