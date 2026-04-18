# Event-Driven Microservices Architecture

This project is a sample production-ready event-driven microservices system built with Java, Spring Boot, and Apache Kafka. It mimics a simplified order processing and payment flow.

## Architecture & Flow

There are 3 microservices communicating asynchronously:

1. **Order Service** (`:8081`)
   - Receives POST requests to create new orders.
   - Saves order in `PENDING` state to `orders_db` (PostgreSQL).
   - Produces `order-events` to Kafka.
2. **Payment Service** (`:8082`)
   - Consumes `order-events` to process payments.
   - Randomly succeeds or fails payment simulation.
   - Saves payment record to `payments_db` (PostgreSQL).
   - Produces `payment-events` to Kafka.
3. **Notification Service** (`:8083`)
   - Consumes `payment-events` and logs simulated email outputs based on payment success/failure.

### Infrastructure
- **Apache Kafka** (Broker on port `9092` internal / `29092` external).
- **Zookeeper** (port `2181`).
- **Kafka UI** (GUI on port `8080` to manage and inspect Kafka topics/messages).
- **PostgreSQL** (port `5432` with databases `orders_db` and `payments_db` automatically provisioned via `init.sql`).

## Running the Application

This repository includes a `docker-compose.yml` for seamless deployment. Requirements: **Docker** and **Docker Compose**.

1. Start all containers:
   ```bash
   docker-compose up -d --build
   ```

2. Wait a minute for the databases, kafka, and java applications to startup. Check logs using:
   ```bash
   docker-compose logs -f
   ```

## Testing the Flow

1. **Create an Order** setup a POST request to Order Service:
   ```bash
   curl -X POST http://localhost:8081/api/orders \
        -H "Content-Type: application/json" \
        -d '{"customerId": "CUST-100", "item": "Premium Subscription", "quantity": 1, "price": 99.99}'
   ```

2. **Check Logs**:
   Look at the logs for Payment Service (`docker logs payment-service`) and Notification Service (`docker logs notification-service`) to see the event flow in real time!

3. **Check Kafka UI**:
   Navigate to [http://localhost:8080](http://localhost:8080) and inspect the `order-events` and `payment-events` topics.

## Reliability & Resiliency built-in
- **Idempotency**: Producer idempotency (`enable.idempotence=true`) is configured to avoid duplicating messages if a network failure occurs during `ack`.
- **Automatic Topics**: Topics are generated automatically on application startup by Spring Kafka Admin.
- **Monitoring**: All apps have `spring-boot-starter-actuator` and Micrometer Prometheus registry configured (accessible via `/actuator/prometheus`).