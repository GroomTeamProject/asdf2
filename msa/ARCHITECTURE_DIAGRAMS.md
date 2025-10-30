# MSA Service Architecture Visualizations

## Overview
```mermaid
graph TD
    subgraph Gateway
        GW[API Gateway]
    end

    subgraph Services
        ORD[Order Service]
        PAY[Payment Service]
        USR[User Service]
        OWN[Owner Service]
        REV[Review Service]
        DEL[Delivery Service]
        NOTIF[Notification Service]
    end

    subgraph Shared
        KAFKA[(Kafka Broker)]
        RDS[(MariaDB RDS)]
        S3[(AWS S3)]
        AUTH[JWT/Secrets Manager]
        DISC[Service Discovery (Cloud Map)]
    end

    GW -->|Routes| ORD
    GW --> PAY
    GW --> USR
    GW --> OWN
    GW --> REV
    GW --> DEL
    GW --> NOTIF

    ORD -- events --> KAFKA
    PAY -- events --> KAFKA
    REV -- events --> KAFKA
    DEL -- events --> KAFKA
    NOTIF -- consumes --> KAFKA

    ORD -.->|Feign| USR
    ORD -.-> OWN
    ORD -.-> PAY
    PAY -.-> ORD
    DEL -.-> ORD

    ORD --> RDS
    PAY --> RDS
    USR --> RDS
    OWN --> RDS
    REV --> RDS
    DEL --> RDS
    NOTIF --> RDS

    OWN --> S3
    USR --> S3

    ORD --> DISC
    PAY --> DISC
    USR --> DISC
    OWN --> DISC
    REV --> DISC
    DEL --> DISC
    NOTIF --> DISC
    GW --> DISC

    GW --> AUTH
    ORD --> AUTH
    PAY --> AUTH
    USR --> AUTH
    OWN --> AUTH
    REV --> AUTH
    DEL --> AUTH
    NOTIF --> AUTH
```

## API Gateway
```mermaid
graph LR
    subgraph Gateway
        GW[Spring Cloud Gateway]
        FILTERS[Global Filters]
        ROUTER[Route Definitions]
        CORS[CORS Config]
        LOG[WebFlux Logging]
    end

    ENV[Env Vars / Secrets Manager]

    ENV --> ROUTER
    ROUTER --> GW
    FILTERS --> GW
    CORS --> GW
    LOG --> GW

    GW -->|Proxy| Client[External Clients]
    GW -->|Forward| Services{{Downstream Services}}
```

## Order Service
```mermaid
graph TD
    subgraph Presentation
        OC[OrderController]
    end

    subgraph Application
        OS[OrderService]
        FQ[Feign Clients]
        EVT[OrderEventPublisher]
    end

    subgraph Domain
        ORDER[Order Aggregate]
        STATE[State Transition Logic]
    end

    subgraph Infrastructure
        REPO[OrderRepository]
        KAFKA[KafkaTemplate]
        DB[(MariaDB)]
        PROM[Prometheus Metrics]
    end

    OC --> OS
    OS --> ORDER
    OS --> STATE
    OS --> REPO
    OS --> FQ
    OS --> EVT
    REPO --> DB
    EVT --> KAFKA
    OS --> PROM
```

## Payment Service
```mermaid
graph TD
    subgraph Presentation
        PC[PaymentController]
    end

    subgraph Application
        PS[PaymentService]
        TOSS[TossClient]
        FEIGN[OrderFeignClient]
        PEVT[PaymentEventPublisher]
    end

    subgraph Domain
        PAYMENT[Payment Aggregate]
        POLICY[Duplicate Guard / Timeout]
    end

    subgraph Infrastructure
        PREPO[PaymentRepository]
        KAFKA[KafkaTemplate]
        DB[(MariaDB)]
        PROM[Prometheus]
    end

    PC --> PS
    PS --> PAYMENT
    PS --> POLICY
    PS --> PREPO
    PS --> FEIGN
    PS --> TOSS
    PS --> PEVT
    PREPO --> DB
    PEVT --> KAFKA
    PS --> PROM
```

## User Service
```mermaid
graph TD
    subgraph Presentation
        UC[UserController]
    end

    subgraph Application
        US[UserService]
        AUTH[AuthService]
        RATE[RateLimiter]
        FILE[FileFacade]
    end

    subgraph Domain
        USER[User Aggregate]
        ADDRESS[Address Value Objects]
    end

    subgraph Infrastructure
        UREPO[UserRepository]
        S3[(AWS S3)]
        DB[(MariaDB)]
        JWT[JWT Provider]
        KAFKA[Kafka Consumer]
    end

    UC --> US
    US --> USER
    US --> ADDRESS
    US --> UREPO
    US --> FILE
    US --> AUTH
    AUTH --> JWT
    FILE --> S3
    UREPO --> DB
    RATE --> UC
    US --> KAFKA
```

## Owner Service
```mermaid
graph TD
    subgraph Presentation
        SC[StoreController]
        MC[MenuController]
    end

    subgraph Application
        SS[StoreService]
        MS[MenuService]
        AUTH[OwnerAuthService]
        VALID[Validation Layer]
    end

    subgraph Domain
        STORE[Store Aggregate]
        MENU[Menu Aggregate]
        CATEGORY[Category Entities]
    end

    subgraph Infrastructure
        SREPO[StoreRepository]
        MREPO[MenuRepository]
        S3[(AWS S3)]
        DB[(MariaDB)]
        KAFKA[KafkaTemplate]
    end

    SC --> SS
    MC --> MS
    SS --> STORE
    SS --> SREPO
    SS --> AUTH
    MS --> MENU
    MS --> CATEGORY
    MS --> MREPO
    SS --> KAFKA
    MS --> KAFKA
    SREPO --> DB
    MREPO --> DB
    SS --> S3
    MS --> S3
```

## Review Service
```mermaid
graph TD
    subgraph Presentation
        RC[ReviewController]
    end

    subgraph Application
        RS[ReviewService]
        FEIGN[Order/User Feign]
        EVENT[ReviewEventPublisher]
    end

    subgraph Domain
        REVIEW[Review Aggregate]
        COMMENT[Owner Reply]
    end

    subgraph Infrastructure
        RREPO[ReviewRepository]
        KAFKA[KafkaTemplate]
        DB[(MariaDB)]
        PROM[Prometheus]
    end

    RC --> RS
    RS --> REVIEW
    RS --> COMMENT
    RS --> RREPO
    RS --> FEIGN
    RS --> EVENT
    RREPO --> DB
    EVENT --> KAFKA
    RS --> PROM
```

## Delivery Service
```mermaid
graph TD
    subgraph Presentation
        DC[DeliveryController]
    end

    subgraph Application
        DS[DeliveryService]
        FEIGN[OrderFeignClient]
        MAPPER[MapStruct Mappers]
    end

    subgraph Domain
        DELIVERY[Delivery Aggregate]
        RIDER[Rider Entities]
        STATS[Statistics]
    end

    subgraph Infrastructure
        DREPO[DeliveryRepository]
        HREPO[HistoryRepository]
        KAFKA[KafkaTemplate]
        DB[(MariaDB)]
        PROM[Prometheus]
    end

    DC --> DS
    DS --> DELIVERY
    DS --> RIDER
    DS --> STATS
    DS --> DREPO
    DS --> HREPO
    DS --> FEIGN
    DS --> MAPPER
    DREPO --> DB
    HREPO --> DB
    DS --> KAFKA
    DS --> PROM
```

## Notification Service
```mermaid
graph TD
    subgraph Presentation
        NC[NotificationController]
        SSE[SSEController]
    end

    subgraph Application
        NS[NotificationService]
        SSESV[SSEConnectionService]
        SCHED[Heartbeat Scheduler]
    end

    subgraph Domain
        NOTIF[Notification Aggregate]
        TEMPLATE[Template Builder]
    end

    subgraph Infrastructure
        NREPO[NotificationRepository]
        KAFKA[KafkaListener]
        DB[(MariaDB)]
    end

    NC --> NS
    SSE --> SSESV
    NS --> NOTIF
    NS --> TEMPLATE
    NS --> NREPO
    NS --> SSESV
    SSESV --> SCHED
    NREPO --> DB
    KAFKA --> NS
```
