
# Interfaces vs Abstract Classes — When to Use What?

Think of **interfaces** and **abstract classes** as blueprints:

- **Interfaces** define *what a thing can do* — a capability/contract.
- **Abstract classes** define *what a thing is* — a partially built type with shared state/behavior.

This guide explains the differences with **clear rules of thumb** and a **full enterprise-grade example** (payments + order processing) in **Java 21** style.

---

## 🔹 Interfaces — “What a thing can do”

An **interface** defines capabilities/behaviors without prescribing how they’re implemented. Different, unrelated classes can all agree to the same contract.

### Real-world analogy
Imagine a delivery service:
- A `Deliverable` thing must be able to `deliver()`
- It doesn’t matter whether it’s a human courier, a drone, or an autonomous car — all must satisfy the same behavior.

```java
public interface Deliverable {
    void deliver(String item);
}

public final class DroneDelivery implements Deliverable {
    @Override public void deliver(String item) {
        System.out.println("Delivering " + item + " by drone.");
    }
}

public final class HumanCourier implements Deliverable {
    @Override public void deliver(String item) {
        System.out.println("Delivering " + item + " by human courier.");
    }
}
```

### When to use an interface
Use an interface when:
- You want multiple **unrelated** classes to share a behavior
- You want to **enforce a contract** (compile-time checks)
- You need **multiple inheritance of type** (a class can implement many interfaces)
- You **don’t** need common state (fields)

### Typical real-world interfaces
`Payable`, `Authenticable`, `Storable`, `Runnable`, `Retryable`, `Auditable`.

### Interface features to remember (Java 8+)
- **Default methods** allow providing default behavior without breaking implementers.
- **Static methods** are great for helpers tied to the capability.
- **Sealed interfaces** (Java 17+) can restrict who can implement, useful for *closed* hierarchies.

```java
public sealed interface PaymentGateway permits StripeGateway, AdyenGateway {
    Authorization authorize(PaymentRequest request);
    Capture capture(String authorizationId, Money amount);
    Refund refund(String captureId, Money amount);

    default String name() { return this.getClass().getSimpleName(); }

    static boolean isSafeAmount(Money amount) { return amount.value().signum() >= 0; }
}
```

---

## 🔸 Abstract Classes — “What a thing is + default behavior”

An **abstract class** is a partially implemented parent with:
- **Common state** (fields)
- **Shared logic**
- **Default implementations**
- One or more **abstract methods** to be completed by subclasses

It **cannot be instantiated** directly.

### Real-world analogy
Think of a `Vehicle`:
- All vehicles have wheels, fuel types, speed limits
- Each vehicle accelerates differently
- Some things are shared (e.g., braking logic)

```java
public abstract class Vehicle {
    protected int speed;

    public void brake() {
        System.out.println("Braking...");
        speed = Math.max(0, speed - 10);
    }

    public abstract void accelerate();
}

public final class Car extends Vehicle {
    @Override public void accelerate() {
        speed += 20;
        System.out.println("Car accelerating to " + speed + " km/h");
    }
}
```

### When to use an abstract class
Use an abstract class when:
- You want **shared code + structure**
- You want child classes to **reuse** methods
- You want **common state** (fields) and invariants
- Your classes are **closely related** (same family/domain)

### Typical real-world abstract bases
`DatabaseConnection`, `BaseController`, `AbstractKafkaConsumer`, `AbstractPaymentService`, `Animal`.

---

## 🔥 Quick Comparison

| Feature                          | Interface                        | Abstract Class                   |
|----------------------------------|----------------------------------|----------------------------------|
| Multiple inheritance             | ✔ Yes (a class can implement many) | ❌ No (single inheritance only)   |
| Shared state (fields)            | ❌ No                             | ✔ Yes                            |
| Default method implementations   | ✔ Yes (since Java 8)             | ✔ Yes                            |
| Primary use                      | Behaviors / capabilities         | Inheritance + shared code/state  |
| Sealed hierarchy (Java 17+)      | ✔ `sealed interface`             | ✔ `sealed abstract class`        |
| Binary compatibility for changes | Easier with default methods      | Breaking changes more likely     |

> **Rule of thumb**: Start with an **interface** for *capabilities*. Introduce an **abstract base** later when you discover *shared state/logic* across a related family.

---

## 🧩 Enterprise Scenario: Payments in Order Processing (Java 21)

We’ll model a simplified, production-flavored payment flow typical in retail/e‑commerce. We’ll use **interfaces** for *capabilities* and an **abstract class** for *shared orchestration and cross‑cutting concerns*.

### 1) Core domain types

```java
public record Money(java.math.BigDecimal value, String currency) {
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) throw new IllegalArgumentException("Currency mismatch");
        return new Money(this.value.add(other.value), this.currency);
    }
}

public record PaymentRequest(String orderId, Money amount, String method, String customerId) {}
public record Authorization(String id, String gateway, boolean success, String message) {}
public record Capture(String id, String authorizationId, boolean success, String message) {}
public record Refund(String id, String captureId, boolean success, String message) {}
```

### 2) Capability: `PaymentGateway` (interface)

Different providers (Stripe, Adyen, InternalMock) implement the same contract.

```java
public sealed interface PaymentGateway permits StripeGateway, AdyenGateway, MockGateway {
    Authorization authorize(PaymentRequest request);
    Capture capture(String authorizationId, Money amount);
    Refund refund(String captureId, Money amount);

    default String name() { return this.getClass().getSimpleName(); }
}

public final class StripeGateway implements PaymentGateway {
    @Override public Authorization authorize(PaymentRequest request) {
        // Call out to Stripe API (omitted). Map response to Authorization.
        return new Authorization("auth_stripe_123", "Stripe", true, "OK");
    }
    @Override public Capture capture(String authorizationId, Money amount) {
        return new Capture("cap_stripe_456", authorizationId, true, "OK");
    }
    @Override public Refund refund(String captureId, Money amount) {
        return new Refund("ref_stripe_789", captureId, true, "OK");
    }
}

public final class AdyenGateway implements PaymentGateway {
    @Override public Authorization authorize(PaymentRequest request) {
        return new Authorization("auth_adyen_123", "Adyen", true, "OK");
    }
    @Override public Capture capture(String authorizationId, Money amount) {
        return new Capture("cap_adyen_456", authorizationId, true, "OK");
    }
    @Override public Refund refund(String captureId, Money amount) {
        return new Refund("ref_adyen_789", captureId, true, "OK");
    }
}

public final class MockGateway implements PaymentGateway {
    @Override public Authorization authorize(PaymentRequest request) {
        return new Authorization("auth_mock", "Mock", true, "SIMULATED");
    }
    @Override public Capture capture(String authorizationId, Money amount) {
        return new Capture("cap_mock", authorizationId, true, "SIMULATED");
    }
    @Override public Refund refund(String captureId, Money amount) {
        return new Refund("ref_mock", captureId, true, "SIMULATED");
    }
}
```

### 3) Cross-cutting concerns: `AbstractPaymentService` (abstract class)

Implements **template method** pattern for common steps (validation, metrics, retries, auditing), while delegating gateway-specific operations.

```java
public abstract class AbstractPaymentService {
    protected final PaymentGateway gateway;

    protected AbstractPaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    // Template method for authorizing a payment
    public final Authorization authorize(PaymentRequest request) {
        long start = System.nanoTime();
        validate(request);
        preAuthorizeHook(request);
        Authorization auth = doAuthorize(request);
        postAuthorizeHook(request, auth, System.nanoTime() - start);
        return auth;
    }

    // Template method for capture
    public final Capture capture(String authorizationId, Money amount) {
        validateAmount(amount);
        return doCapture(authorizationId, amount);
    }

    // Template method for refund
    public final Refund refund(String captureId, Money amount) {
        validateAmount(amount);
        return doRefund(captureId, amount);
    }

    // Shared validations and helpers
    protected void validate(PaymentRequest request) {
        if (request == null) throw new IllegalArgumentException("request is null");
        if (request.amount() == null || request.amount().value().signum() < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    protected void validateAmount(Money amount) {
        if (amount == null || amount.value().signum() < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    // Optional hooks for subclasses
    protected void preAuthorizeHook(PaymentRequest request) { /* no-op by default */ }
    protected void postAuthorizeHook(PaymentRequest request, Authorization auth, long nanos) { /* metrics/audit */ }

    // Abstract operations to be provided by concrete services
    protected abstract Authorization doAuthorize(PaymentRequest request);
    protected abstract Capture doCapture(String authorizationId, Money amount);
    protected abstract Refund doRefund(String captureId, Money amount);
}
```

### 4) Concrete services (inherit shared logic)

```java
public final class CardPaymentService extends AbstractPaymentService {
    public CardPaymentService(PaymentGateway gateway) {
        super(gateway);
    }

    @Override protected Authorization doAuthorize(PaymentRequest request) {
        return gateway.authorize(request);
    }

    @Override protected Capture doCapture(String authorizationId, Money amount) {
        return gateway.capture(authorizationId, amount);
    }

    @Override protected Refund doRefund(String captureId, Money amount) {
        return gateway.refund(captureId, amount);
    }
}

public final class WalletPaymentService extends AbstractPaymentService {
    public WalletPaymentService(PaymentGateway gateway) { super(gateway); }

    @Override protected Authorization doAuthorize(PaymentRequest request) {
        // extra wallet-specific checks
        return gateway.authorize(request);
    }

    @Override protected Capture doCapture(String authorizationId, Money amount) {
        return gateway.capture(authorizationId, amount);
    }

    @Override protected Refund doRefund(String captureId, Money amount) {
        return gateway.refund(captureId, amount);
    }
}
```

### 5) Wiring it together (DI-friendly, e.g., Spring)

```java
public final class PaymentsFacade {
    private final CardPaymentService cardPayments;
    private final WalletPaymentService walletPayments;

    public PaymentsFacade() {
        // In a real app, use DI to provide the gateway implementations per environment
        PaymentGateway stripe = new StripeGateway();
        PaymentGateway adyen = new AdyenGateway();

        this.cardPayments = new CardPaymentService(stripe);
        this.walletPayments = new WalletPaymentService(adyen);
    }

    public Authorization authorizeCard(PaymentRequest request) {
        return cardPayments.authorize(request);
    }

    public Authorization authorizeWallet(PaymentRequest request) {
        return walletPayments.authorize(request);
    }
}
```

**Result:**
- The **capability** of processing payments is captured by the **`PaymentGateway` interface**.
- The **shared orchestration** (validation, hooks, template algorithm) lives in the **`AbstractPaymentService`**.
- Concrete services combine both: they **inherit** reusable logic and **delegate** to any gateway implementation.

---

## 🧠 Design Notes & Best Practices

1. **Favor interface-first design** for cross-cutting behaviors used by many unrelated types.
2. Introduce an **abstract base** only when you see real, repeated patterns of state/logic across a cohesive family.
3. Keep abstract bases **small and stable**. Big base classes ossify hierarchies and make change risky.
4. Use **default methods** in interfaces to evolve contracts without breaking existing implementations.
5. Consider **sealed interfaces/classes** to model closed taxonomies (e.g., a finite set of payment statuses) and get better exhaustiveness checks in `switch` expressions.
6. Prefer **composition over inheritance** when sharing is incidental, not essential (e.g., inject a `RetryPolicy` instead of stuffing retry logic into a base class).
7. For frameworks (e.g., Spring), design **SPI via interfaces** (what the app can plug in) and **Skeletal implementations** via abstract bases (the template).

---

## ✅ Quick Decision Checklist

- **Do I only need a contract/capability?** → **Interface**
- **Do I also need shared state/logic/invariants?** → **Abstract class**
- **Do implementations span unrelated classes or modules?** → **Interface**
- **Are implementations a tight, related family?** → **Abstract class**
- **Do I need multiple inheritance of type?** → **Interface**
- **Will the contract evolve frequently?** → Prefer **Interface** with default methods

---

## 🛑 Common Pitfalls

- **God abstract classes**: Too much logic in a base class couples everyone to one evolution path.
- **Leaky interfaces**: Interfaces exposing persistence/logging internals defeat decoupling.
- **Accidental inheritance**: Extending a base just to reuse a single method — prefer composition.
- **Binary compatibility gotchas**: Adding abstract methods to a widely used base class is breaking; prefer new defaults on interfaces.

---

## 🧭 Bonus: Sealed Types in Java 17+

Use **sealed interfaces/abstract classes** when you need a closed set of implementations (e.g., limited payment methods approved by compliance). This helps maintainers and improves compiler checks.

```java
public sealed abstract class PaymentResult permits Approved, Declined, Pending {}
public final class Approved extends PaymentResult {}
public final class Declined extends PaymentResult {}
public final class Pending  extends PaymentResult {}
```

Then:

```java
static String toMessage(PaymentResult result) {
    return switch (result) {
        case Approved a -> "Approved";
        case Declined d -> "Declined";
        case Pending  p -> "Pending";
    };
}
```

---

## 🧪 Minimal Usage Example (End-to-End)

```java
public class Demo {
    public static void main(String[] args) {
        PaymentGateway gateway = new StripeGateway();
        AbstractPaymentService service = new CardPaymentService(gateway);

        PaymentRequest request = new PaymentRequest("ORD-1001", new Money(new java.math.BigDecimal("49.99"), "GBP"), "CARD", "CUST-42");
        Authorization auth = service.authorize(request);
        System.out.println("Authorized: " + auth);

        Capture cap = service.capture(auth.id(), request.amount());
        System.out.println("Captured:  " + cap);

        Refund ref = service.refund(cap.id(), new Money(new java.math.BigDecimal("10.00"), "GBP"));
        System.out.println("Refunded:  " + ref);
    }
}
```

---