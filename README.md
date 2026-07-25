<div align="center">

# Event Consumer : Sports Events Consumer And Bet Settlement Engine

#### Event-driven Spring Boot service that consumes sports events with its related data(event, teams, markets, prices, and scores), inserts them into respective tables,evaluates open bet slips against matches outcomes, determines win/loss/void outcomes across three market types, and emits wallet transactions correctly, in order, without double-counting.

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?logo=springboot&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?logo=rabbitmq&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Tests](https://img.shields.io/badge/tests-220%20passing-brightgreen)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

</div>

---

## System context

This service is one of three in a sportsbook microservices platform:

- **[rapid-engine](https://github.com/bicosteve/rapid-engine)** : odds 
  management, market creation, bet slip ingestion
- **event-consumer** : sports and result events ingestion for bet settlement and
  wallet transaction emission
- **[api-gateway](https://github.com/bicosteve/api-gateway)** : 
  public-facing for auth, events, and bet placementAPI surface

`event-consumer` sits downstream of `rapid-engine` on the results queue. When a match finishes, `rapid-engine` publishes the result; this service picks it up, evaluates every open slip against it, and emits a `BetStatusUpdate` to the transactions queue for the wallet service to process.

---

## What it does

`rapid-engine` publishes sports events and final scores through the broker selected by `MESSAGING_BROKER` (`rabbitmq` or `kafka`). This service:

1. Consumes the sport's events.
2. Consumes the results events.
3. Updates the events markets, scores, prices(odds).
4. Loads every open bet and its constituent slips for that match
5. Evaluates each slip against the score using the appropriate market strategy 
   (moneyline, handicap, totals)
6. Applies a deterministic 4-rule precedence to resolve the overall bet outcome
7. Publishes a `BetStatusUpdate` to the transactions queue
8. The wallet service settles the payout

The hard part is step 4. With possible hundreds of slips per bet across 
multiple games some finished, some still live the resolution logic has to be correct, ordered, and idempotent. That's what most of the test suite covers.

---

## Architecture

```
                ┌──────────────────────────────────────────────────────┐
                │                      RABBITMQ                        │
                │                                                      │
   rapid-engine ──▶  events.queue    results.queue                     │
                │        │                 │         transactions.queue ──▶ wallet service
                └─────────│─────────────────│─────────────▲────────────┘
                          │                 │             │
                          ▼                 ▼             │
                   ┌─────────────-┐   ┌─────────────┐     │
                   │EventsConsumer│  │ResultConsumer│     │
                   └──────┬──────-┘   └──────┬──────┘     │
                          │                 │             │
                          ▼                 ▼             │
                   ┌─────────────┐   ┌─────────────┐      │
                   │EventService │   │ResultService │─────┘
                   │(persist +   │   │(evaluate +   │  publishes
                   │ normalize)  │   │ settle)      │  BetStatusUpdate
                   └─────────────┘   └──────┬───────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │  Strategy   │──▶ MoneylineEvaluator
                                     │  Resolver   │──▶ HandicapEvaluator
                                     │             │──▶ TotalsEvaluator
                                     └─────────────┘
```

Every exchange, queue, routing key, Kafka topic, and group is environment-driven; the same artifact runs unchanged across dev, staging, and production.

### Broker selection and ownership

Set `MESSAGING_BROKER=kafka` or `MESSAGING_BROKER=rabbitmq`; exactly the selected transport listeners and publisher are created. Kafka uses `spring.kafka.bootstrap-servers` (`KAFKA_BOOTSTRAP_SERVERS`). Results use an explicit string wire contract; matches and transaction updates remain JSON with their existing keys. `KAFKA_CREATE_TOPICS=true` creates the three input topics and their three-partition `.DLT` topics for local development; production defaults to `false`, so infrastructure must provision `matches.queue`, `results.queue`, and `transactions.queue`, plus their derived `matches.queue.DLT`, `results.queue.DLT`, and `transactions.queue.DLT` topics, each with three partitions. Kafka retries twice with a one-second fixed backoff before publishing to the corresponding `.DLT`.

Rabbit uses Spring Boot's `spring.rabbitmq` connection configuration, including `RABBITMQ_SSL_ENABLED` for TLS. Publishers use correlated confirms, mandatory routing, returns, and `RABBITMQ_PUBLISHER_TIMEOUT_MILLIS`; nacks, returns, and confirm timeouts synchronously fail the caller. Each matches/results/transactions channel has independently configured `*_DLX`, `*_DLK`, and `*_DLQ` values. Listeners retry twice after the first attempt and reject without requeueing, allowing the configured broker DLX to route failures to that channel's DLQ.

Local host processes use Kafka at `localhost:29092`; the Compose application overrides that host setting with `kafka:9092`. RabbitMQ is `localhost:5672` (management UI: `localhost:15672`). Since Compose always runs both brokers, the app starts only after MySQL, Kafka, and RabbitMQ are healthy.

---

## Engineering Decisions

### Strategy pattern for market evaluation

Adding a new market type e.g corners, first goalscorer, Asian handicap etc
requires one new class and one Spring bean. The dispatch is keyed by a `Map<String, MarketEvaluator>` injected by name:

```java
// ResultService.java
String marketName = slip.getMarketName().toLowerCase().trim();
String strategyKey = marketName + "Evaluator";
MarketEvaluator strategy = marketEvaluator.get(strategyKey);
return strategy.evaluate(slip, score);
```

The file you touch to add a market is exactly one class. No switch statements, no conditionals to update elsewhere.

| Strategy | Handles |
|---|---|
| `MoneylineEvaluator` | Who won outright |
| `HandicapEvaluator` | Did the spread cover |
| `TotalsEvaluator` | Over/under the line |

---

### The 4 bet resolution rules

When a match finishes, every affected bet is re-evaluated through a 
deterministic, order-sensitive precedence. It encodes how a real product logic 
on a multi-game accumulator should behave when some legs are still live:

```java
// ResultService.checkBetStatus — order is the contract.
1. Any LOST slip      →  bet is LOST     (one loss kills the whole bet)
2. Any PENDING slip   →  bet stays PENDING (wait for remaining games)
3. ALL slips VOID     →  bet is VOID     (full refund)
4. Otherwise (WON + VOID only, no losses, no pending) → WON
```

Rule 1 before Rule 2 matters: a bet with one lost leg and one pending leg is 
already lost. There's no point waiting for the second game.

---

### JdbcTemplate over JPA

Every repository uses `JdbcTemplate` directly. No lazy-loading surprises. No N+1 queries introduced by a misconfigured fetch strategy. No dirty-checking behaviour to reason about at settlement time.

Every SQL statement the application executes is visible in the source:

```bash
grep -r "jdbcTemplate" src/
```

In a financial settlement service that fans out across thousands of slips, that transparency is not optional.

---

### 8-state event lifecycle

`EventStatus` is a proper state machine, not a boolean:

```
SCHEDULED → IN_PROGRESS → (HALFTIME ↔ IN_PROGRESS) → END_PERIOD → FINAL
                                    │
                                    ├── DELAYED → IN_PROGRESS
                                    ├── POSTPONED (→ re-scheduled insert)
                                    ├── SUSPENDED
                                    ├── FORFEIT
                                    └── CANCELED
```

`isVoidStatus()` in `ResultService` handles `CANCELED`, `POSTPONED`, `SUSPENDED`, and `FORFEIT` encoded once, tested once, referenced everywhere that needs it.

---

### CI that enforces quality, not just runs tests

`.github/workflows/ci.yml` runs three chained jobs:

1. **`test`** — `./mvnw test`, then a Python script parses the Surefire XML and blocks the build if any test regresses from the current baseline. `@Disabled` tests are excluded from the denominator so they cannot be used to game the pass rate.
2. **`build`** — only runs if `test` passes. Packages the JAR and uploads it as a build artifact.
3. **`docker`** — for containerization, wired to publish the image to the 
   registry.

Surefire reports and the full Maven log are uploaded on every run, including failures. A broken CI is one click away from diagnosis.

---

## Test coverage

```
[INFO] Tests run: 220, Failures: 0, Errors: 0, Skipped: 1
```

The suite runs in ~10 seconds with no infrastructure dependencies — no Docker, no RabbitMQ, no MySQL. Unit tests are a feedback loop, not a deployment exercise.

| Layer | What is covered |
|---|---|
| Enums | All 4 enums — every constant and every meaningful transition |
| Models | 11 domain models — constructors, builders, getters/setters, Lombok edge cases |
| Services | `EventService`, `ResultService`, `TransactionService`, `WalletService` every rule, every branch, every no-op-when-empty path |
| Consumers | All 3 RabbitMQ consumers with mocked channels and payloads |
| Evaluators | All 3 market strategies against realistic score scenarios |
| Producers | `TransactionProducer` payload assembly |
| Events | `BetStatusUpdate` DTO contract |
| Integration | `MainTests` is `@Disabled` kept as a staging smoke test requiring live MySQL and RabbitMQ |

```bash
./mvnw test
```

---

## Production schema migration

Production uses `DB_MODE=never`, so `schema.sql` does not evolve an existing managed database. Before enabling event-cache reconciliation in production, run the idempotent MySQL 8 migration once (it is also safe to run repeatedly):

```bash
mysql --defaults-extra-file=/secure/path/event-consumer.cnf < db/migrations/20260722_add_rundown_event_date_id_index.sql
```

The migration adds `idx_rundown_event_date_id(event_date,event_id)` only when it is absent. Verify it before deployment with:

```sql
SHOW INDEX FROM rundown_event WHERE Key_name = 'idx_rundown_event_date_id';
```

Fresh development databases receive the same index from `src/main/resources/schema.sql`.

---

## Running locally

**Prerequisites:** Java 21, Maven 3.9+ (or `./mvnw`), a `.env` file

```bash
git clone https://github.com/bicosteve/event-consumer.git
cd event-consumer
cp .env-example .env       # fill in DB and RabbitMQ credentials
./mvnw test                # 220 tests, no infrastructure required
./mvnw spring-boot:run     # starts the consumer
```

**Example `.env`:**

```env
DB_URL=jdbc:mysql://localhost:3306/event_consumer
DB_USERNAME=root
DB_PASSWORD=secret
DB_MODE=always

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

RABBITMQ_MATCHES_EXCHANGE=sports.matches
RABBITMQ_MATCHES_QUEUE=event-consumer.matches
RABBITMQ_MATCHES_ROUTING_KEY=match.*

APP_PORT=8080
LOGGING_LEVEL=DEBUG
```

---

## Project structure

```
src/
├── main/java/com/bix/event_consumer/
│   ├── consumers/        # RabbitMQ @RabbitListener entry points
│   ├── producer/         # Outbound RabbitTemplate publisher
│   ├── services/         # Business logic ResultService is the core
│   ├── repositories/     # JdbcTemplate data access
│   ├── evaluator/        # MarketEvaluator interface + impl/
│   ├── models/           # Domain objects (Event, Bet, Slip, Score...)
│   ├── events/           # Internal DTOs (BetStatusUpdate)
│   ├── enums/            # BetStatus, EventStatus, SlipStatus, TransactionType
│   ├── rabbitmq/         # Exchange/queue/binding beans and config
│   └── config/           # Jackson and application config
├── main/resources/
│   ├── application.yaml          # imports .env
│   ├── application-dev.yaml      # DB and RabbitMQ wiring
│   ├── schema.sql                # bootstrap DDL
│   └── event-consumer-logback.xml
└── test/java/com/bix/event_consumer/
    ├── enums/  models/  services/  consumers/  producer/  evaluator/impl/  events/
```

---

## Tech stack

**Java 21** · Spring Boot 3.5 · RabbitMQ · MySQL 8 · Lombok · JUnit 5 · Maven · GitHub Actions

---

## Roadmap

- [x] Event ingestion from RabbitMQ (`EventsConsumer` → `EventService`)
- [x] Result ingestion and bet settlement (`ResultConsumer` → `ResultService`)
- [x] Transaction event emission (`BetStatusUpdate` → `TransactionProducer`)
- [x] Strategy pattern for Moneyline, Handicap, and Totals markets
- [x] 220+ unit tests, deterministic, no infrastructure required
- [x] CI gate with Surefire report artifacts uploaded on every run
- [ ] **Outbox pattern** for wallet transaction publishing — currently the publish happens in-process after settlement; adding the outbox removes the gap where a crash between settlement and publish could result in a missed transaction
- [ ] **Idempotent consumers** with Redis-backed deduplication keys — prevents double-settlement if a message is redelivered after a consumer restart
- [x] **Docker image** — pipeline job is wired and ready
- [ ] **Replay tool** — re-run a finished game's result against a stored event snapshot for debugging and audit
- [ ] **Metrics** — Micrometer and Prometheus for settlement latency per market type

---

## License

MIT — see `LICENSE` for details.