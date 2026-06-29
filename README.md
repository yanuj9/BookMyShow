# Scalable Ticket Booking Engine (BookMyShow Architecture)

A high-throughput, concurrent ticket booking engine built in Java, designed to demonstrate enterprise-level distributed system patterns. This project tackles the classic "Flash Sale / Double Booking" anomaly by implementing a hybrid concurrency model using Redis Distributed Locks and deterministic thread synchronization.

## 🚀 Core Architecture & Concurrency Strategy

In high-demand ticket booking systems (e.g., Avengers movie releases, concerts), thousands of users may attempt to book the exact same seat simultaneously. Relying solely on Database-level locking (Pessimistic or Optimistic) during a flash sale leads to exhausted connection pools and massive CPU overhead due to transaction rollbacks. 

This engine solves the **Lost Update Anomaly** by offloading concurrency control to a caching layer:

1. **Redis Gatekeeper (Fail-Fast Mechanism):** Threads must acquire an atomic distributed lock via Redisson before ever touching the database. This protects the database from traffic spikes by instantly rejecting competing requests in-memory.
2. **Read Committed Double-Check:** Once a thread secures the Redis lock, it enters the database transaction to verify the seat's availability on disk, protecting against edge cases like premature cache evictions or clock drift.
3. **Deterministic Thread Testing:** The simulation engine utilizes `CountDownLatch` (a multi-latch barrier pattern) to pause the JVM thread scheduler and force exactly 10 concurrent threads to hit the Redis lock at the exact same microsecond, proving the thread safety of the architecture.

## 🛠 Tech Stack
* **Language:** Java 17+
* **Concurrency:** Java `java.util.concurrent` (Executors, CountDownLatch, AtomicInteger)
* **Distributed Cache / Locking:** Redis, Redisson Client (Lua-script based atomic locks)
* **Design Patterns:** Domain-Driven Design (DDD), Dependency Inversion Principle (DIP), Repository Pattern
* **Infrastructure:** Docker & Docker Compose (for local Redis/DB containerization)
* **Build Tool:** Apache Maven

## 📂 Project Structure
* `domain/` - Pure business entities (`City`, `Theatre`, `Screen`, `Show`, `Seat`).
* `concurrency/` - Abstracted `DistributedLockManager` interface and Redisson implementations.
* `repository/` - Data access layer interfaces allowing seamless swapping between `InMemory`, `PostgreSQL`, or other databases without altering business logic.
* `service/` - Orchestration layer (`BookingService`, `TheatreService`) handling transactions and lock acquisition.
* `App.java` - The master simulation engine running the multi-threaded flash sale test.

## 🔮 Future Extensibility (Roadmap)
* `Redisson MultiLock:` Upgrading the single string lock to atomic MultiLocks for multi-seat bookings to prevent composite key overlaps and distributed deadlocks.

* `PostgreSQL Integration:` Replacing the InMemoryShowRepository with the PostgresShowRepository utilizing HikariCP for connection pooling and JDBC batching for high-performance upserts.

## ⚙️ How to Run Locally

1. **Start the Infrastructure (Redis):**
   ```bash
   docker-compose up -d