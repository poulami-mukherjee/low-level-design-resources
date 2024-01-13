# Trade Republic Coding Challenge Solution Documentation

## Objective
This Java Spring Boot Project  was designed and built to provide solutions for the Coding Challenge Round of Trade Republic.

## Challenge Statement
Develop a system that enables users to view price histories in the form of [candlestick](https://en.wikipedia.org/wiki/Candlestick_chart) for a particular stock aka. instrument, which has a unique identifier (`isin`).

## Functional Requirements
1. System should receive continuous updates from a Partner service, 
    1. Establish Websocket connection with Partner Service.
    2. Ingest instrument addition/deletion message events
    3. Ingest price change event called `Quote` 
2. Persists the ingested data into relevant DataStores
3. Generate `Candlesticks` for a user specified instrument using a service endpoint.
    1. Create an endpoint using which client can interact with our service
    2. It should include the most recent prices.
    3. Find the maximum and minimum price for an instrument for any minute and aggregate and store it in the form of `Candlestick`
    4. It should provide a 30 minutes price or quote history in the form of `CandleSticks`
    5. If there weren't any quotes received for more than a minute, instead of missing candlesticks for that minute values from the previous candle are reused.

## Non Functional Requirements
1. Data cleanup on websocket reconnection
2. Synchronisation and concurrency
3. Proper garbage disposal and efficient resource utilisation.
4. Minimise Latency
5. Input Validation and Non-null
6. Proper Error Handling
7. Proper Logging for efficient error debugging

##  Assumption
1. We will always receive [TextMessage](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/socket/TextMessage.html) streams from the PartnerService in the [JSON](https://en.wikipedia.org/wiki/JSON) format.
2. Assuming the value of "type" received from Partner Service is a String. We need to convert the String to type Enum to support our design.
3. In memory DataStores will be cleaned up every 2 hours and on reconnection with Partner Service.

## Out of Scope
1. Although, we receive the `description` data for instruments from the Partner Service, but we're not storing it because it's not required for our current project goals.
2. Refer to [Enhancement Section](#future-enhancements) 

## Model

### Input 

Once started, Partner Service provides two websocket streams (`ws://localhost:8032`) - `/instruments` and `/quotes`, plus a website preview of how the stream look (http://localhost:8032). 

**NOTE** - Click here to refer to [Assumptions Section](#-assumption) for assumptions made on the type of data broadcast by Partner Service.

- Data formats of ADD and DELETE instruments message events from `/instruments` endpoint:
  ```
  // ADD EVENT STRUCTURE
  {
     "data": {
     "description": "luptatum senectus noluisse nulla dictumst",
     "isin": "IU1I5C5N8300"
     },
     "type": "ADD"
  }
  // DELETE EVENT STRUCTURE
  {
     "data": {
         "description": "luptatum senectus noluisse nulla dictumst",
         "isin": "IU1I5C5N8300"
     },
     "type": "DELETE"
  }
  ```
- Data formats of QUOTE message events from `/quotes` endpoint:
  ```
  {
     "data": {
          "price": 452.2828,
          "isin": "QV532Y814406"
     },
     "type": "QUOTE"
  }
  ```
### Enums
1. **Type**

    Represents the types of events that can occur within the system, particularly in relation to financial instruments or quotes.
   
    *Values:*
   
     - ADD: Indicates an addition event, such as adding a new financial instrument or quote. 
     - DELETE: Represents a deletion event, typically used when removing an existing instrument or quote. 
     - QUOTE: Signifies an event related to a financial quote, possibly indicating a new or updated quote.
      
   *Usage:* Used throughout the system to categorize and handle different types of events, ensuring that each event is processed according to its specific type.
2. **Status**
    Defines the status of financial instruments within the system.
   
    *Values:*
     - ACTIVE: Indicates that a financial instrument is active and relevant for current operations. 
     - INACTIVE: Signifies that a financial instrument is no longer active, either due to deletion or other reasons.

    *Usage:* Essential for managing the lifecycle of financial instruments, allowing the system to differentiate between active and inactive instruments for various processing needs.

### Data Model Classes
1. **Instrument**
   Represent a financial instrument with a unique identifier (ISIN) and a description.

   *Fields:*
   * description: A textual description of the instrument.
   * isin: The International Securities Identification Number (ISIN) for the instrument.
2. **InstrumentEvent**
   Represents a financial instrument addition/deletion event.

   *Fields:*
   * instrument: A financial instrument object
   * type: Enum value denoting ADD/DELETE event
3. **Quote**
   Represents a financial quote, encapsulating the time of the quote and its price.
   
   *Fields:*
   - time: An Instant object representing the timestamp of the quote.
   - price: A Double value indicating the price of the financial instrument at the given time.
   
   *Usage:* Used to store and transmit information about the price of a financial instrument at a specific point in time. This is crucial for financial analysis, tracking market movements, and decision-making processes.
4. **QuoteEvent**

   Encapsulates an event related to a financial quote, linking it with the corresponding financial instrument's ISIN.
   
   *Fields:*
   - isin: A String representing the International Securities Identification Number (ISIN) of the financial instrument. 
   - quote: A Quote object containing the details of the quote associated with the instrument.

   *Usage:* Primarily used in the system to handle and process events related to financial quotes. This includes adding new quotes, updating existing ones, or other quote-related operations.
5. **Candlestick**

   Represents a single candlestick, which is a common way to visualize price movements in financial markets. Each candlestick encapsulates the open, high, low, and close prices for one-minute time period.
   
   *Fields:*
   - openTimestamp: Timestamp marking the beginning of the interval.
   - closeTimestamp: Timestamp marking the end of the interval.
   - openPrice: Price at the start of the interval.
   - highPrice: Highest price during the interval.
   - lowPrice: Lowest price during the interval.
   - closingPrice: Price at the end of the interval.

   *Usage:* Used in the aggregation and analysis of financial data, particularly for visualizing price trends over time.

6. **Candlesticks**

   A collection of candlestick data, stored in a ConcurrentSkipListSet to maintain order and provide thread safety and avoid duplication.
   
   *Fields:*
   - candlesticks: A set of Candlestick objects.

   *Usage:* Used to hold and manage multiple candlestick data points, typically representing the price movement of an instrument over time.

### Data Stores
1. **InstrumentDataStore**
   Manages the status and information of financial instruments.

   *Field:*
    * instrumentDataStore: A concurrent hash map of String isin and their Active/Inactive status denoted by Status Enum for thread-safe operations.

   *Methods:*
    - addInstrument(Instrument instrument): Adds or updates an instrument's status.
    - deleteInstrumentForId(String isin): Marks an instrument as inactive.
    - isInstrumentPresent(String isin): Checks if an instrument is present in the store.
    - cleanupInstrumentDataStore(): Clears all instrument data.

   *Usage:* Essential for tracking the active and inactive status of financial instruments in the system.
2. **QuoteDataStore**
   Handles the storage and retrieval of quote data for financial instruments. It uses a concurrent hash map and skip list set for efficient data management.

   *Methods:*
    - addInstrumentQuote(QuoteEvent quoteEvent): Adds a new quote for an instrument.
    - removeInstrumentQuoteForIsin(String isin): Removes all quotes for a specific instrument.
    - cleanupQuoteDataStore(): Clears all quote data.
    - getQuotesForIsin(String isin): Retrieves quotes for a specific instrument.

   *Usage:* Crucial for storing and accessing real-time and historical quote data for financial analysis.
3. **CandlestickDataStore**

   Manages the storage and retrieval of candlestick data. It uses a concurrent hash map of String isin and ConcurrentSkipListSet of precomputed CandleSticks to ensure thread safety, making it suitable for high-concurrency environments. It also dedupes data.

   *Methods:*
    - cleanupCandlesticksDataStore(): Clears all candlestick data.
    - hasCandlestickDataForIsin(String isin): Checks if candlestick data is available for a specific ISIN.
    - addCandlesticks(String isin, Candlesticks candlesticks): Adds candlesticks for a specific ISIN.
    - getCandlesticks(String isin): Retrieves candlesticks for a specific ISIN.

   *Usage:* Integral for storing and accessing candlestick data associated with various financial instruments.

### Controller

#### CandlestickController

*Candlesticks API*
* **Endpoint** http://localhost:9000/candlesticks?isin={isin}
    
    *Method:* GET

    *Endpoint Params:*
    - Required: isin=[string] - The International Securities Identification Number (ISIN) of the financial instrument.
    
    *HTTP Status Responses:*
    - Success Response:
      - Code: 200 OK
      - Content: A list of candlesticks for the specified ISIN.
    - No Content Response:
      - Code: 204 No Content
      - Content: None (when no candlesticks are available or the instrument does not exist).
    - Error Response:
      - Code: 500 Internal Server Error
      - Content: Error message (in case of internal server errors).


##  Technologies and Framework Used
- Spring Boot Framework 3.1.2 
- Maven as Build Tool
- Java 17
- Spring Websocket-6.1.2
- JSONObject-20210307 for JSON Processing
- Lombok for removing boilerplate code
- jUnit for unit testing

## Getting Started
To build and run a Spring Boot project using Maven and IntelliJ IDEA, follow these steps:

### 1. **Setup**
- Ensure you have [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) installed.
- Install [IntelliJ IDEA](https://www.jetbrains.com/idea/). While there's a Community edition, it's recommended to use the Ultimate edition for full Spring Boot support.

### 2. **Open the Project in IntelliJ IDEA**
- Launch IntelliJ IDEA.
- Select "Open" or "Import" on the welcome screen.
- Navigate to your project's root directory (where the `pom.xml` file is located) and click `OK`.

### 3. **Enable Auto-Import (Optional)**
Once the project is opened, IntelliJ IDEA may prompt you to enable auto-import for Maven. This means that whenever you make changes to your `pom.xml`, IntelliJ will automatically refresh your Maven dependencies. It's a useful feature to keep dependencies in sync.

### 4. **Build the Project**
There are a few ways to build your project within IntelliJ IDEA:
- **Using the Maven Tool Window**:
   - Open the `Maven` tool window, usually located on the right side of the IDE.
   - Navigate to your project and right-click on it.
   - From the context menu, select `Lifecycle` > `clean` and then `Lifecycle` > `install`. This will clean and then build your project.
- **Using the Terminal**:
   - Open the terminal tab (usually located at the bottom of the IDE).
   - Run the following command:
     ```
       mvn clean install
     ```
### 5. **Run the Application**
- Run the JAR of Partner service which broadcasts data streams of `/instruments` and `/quotes` for multiple `isin` via websocket connection.  We need to run given JAR of Partner Service by executing the following command on port `8032`:
    ```
    java -jar partner-service-1.0.1-all.jar --port=8032
    ```
- **Using IntelliJ's Run Button**:
   - Open the main application class (the class annotated with `@SpringBootApplication`).
   - In the gutter (the space to the left of the code), you should see a green triangle (the "Run" icon). Click on it and select `Run 'CandleStickGeneratorPatternService'`.
   - IntelliJ will use the Spring Boot Maven plugin to start the application.
- **Using the Maven Plugin**:
   - In the `Maven` tool window, navigate to `Plugins` > `spring-boot` > `spring-boot:run`.
   - Right-click on `spring-boot:run` and select `Run`.

### 6. **Access the Application**
- Once your application is running, we can access the endpoint by providing an instrument ID or isin through any API Client like [Postman](https://www.postman.com/)
   ```
    <http://localhost:9000?isis={isin}>
   ```
  **NOTE** - Make sure to check your application's configuration if default port or context path is changes

### 7. **Stop the Application**
- If you want to stop the application, you can click on the red square (stop button) in the run window at the bottom of IntelliJ IDEA.

## Test Sample
- **Endpoint:** `http://localhost:9000/candlesticks?isin=IE70141153O6`
- **Data:**
    ```
    [
    {
        "openTimestamp": "2024-01-05 16:18:00",
        "openPrice": 644.0779,
        "highPrice": 644.0779,
        "lowPrice": 644.0779,
        "closePrice": 644.0779,
        "closeTimestamp": "2024-01-05 16:18:59"
    },
    {
        "openTimestamp": "2024-01-05 16:19:00",
        "openPrice": 598.8831,
        "highPrice": 598.8831,
        "lowPrice": 598.8831,
        "closePrice": 598.8831,
        "closeTimestamp": "2024-01-05 16:19:59"
    },
    {
        "openTimestamp": "2024-01-05 16:20:00",
        "openPrice": 345.974,
        "highPrice": 345.974,
        "lowPrice": 345.974,
        "closePrice": 345.974,
        "closeTimestamp": "2024-01-05 16:20:59"
    }
    ]
    ```

## Project Structure
```
CandlestickPatternGenerator
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── tickgenerator
│   │   │           └── accessor
│   │   │               ├── DataStorageAccessor.java                            # Accessor responsible for accessing and managing the DataStores
│   │   │           └── config                                                  # Contains all configuration files containing Spring Beans
│   │   │           └── constant                                                # Contains all Application constants
│   │   │           └── controller                                              # Contains CandleStickController which has the REST API endpoint to compute candlesticks for a particular instrument
│   │   │           └── handler                                                 # Contains logic for handling websocket connection and exception handling
│   │   │           └── helper                                                  # Contains Helper classes for computing and aggregating Candlestick
|   |   |           └── model                                                   # Models or Data Classes. Please Refer Data Classes, Enums and Datastore Section
│   │   │           └── service                                                 # Handles the heavy lifting Business Logic and aggregation
|   |   |       └── CandlestickPatternGeneratorApplication.java                 # Starting point of the Spring Boot Application. Annoted with @SpringBootApplication
│   │   │
│   ├── test                                                                    # Unit test classes reside here
│   │   ├── java
│   │   │   └── com
│   │   │       └── tickgenerator                                               
│   │   │           
│   │   │               
│   │   │                                     
├── .gitignore                                                                  # Git ignore file
├── mvnw and mvnw.cmd (optional)                                                # Maven wrapper scripts
├── pom                                                                         # Contains dependencies, plugins, and other build configurations
```


## Appendix
### Future Enhancements
- Add more unit tests coverage
- Configure the endpoint to accept HTTPS request
- Have Server Side Rate Limiting to prevent DDoS (Distributed Denial of Service) attacks, service overuse, and maintaining the quality of service
- Protection against security vulnerabilities like CSRF or XSS, use HTTPS, use authentication like OAuth or API keys for state-changing APIs
- Proper Logging and Monitoring for Observability
- Retry mechanism for the API for transient failures
- Documentation of the API through Swagger or Open AI

### Design Choices
- Why Spring Websocket and not Java Websocket API?
  - Java WebSocket API: Standard in Java EE, simple, portable, less overhead, limited outside Java EE. 
  - Spring WebSocket: Integrates well with Spring, flexible, feature-rich. 
  - **Used:** Spring WebSocket which is more compatible and integrates well with Spring Boot.
- Choice between using ConcurrentHashmap and ReentrantLock
  - **`ConcurrentHashMap`** is designed to handle concurrent access and modifications efficiently without the need for external synchronization.
    Here are a few points to consider:

    1. **Concurrency Handling in `ConcurrentHashMap`**: **`ConcurrentHashMap`** provides thread-safe operations for common tasks like adding, updating, and removing key-value pairs. It achieves this by internally partitioning the map and locking only a portion of it during updates. This allows high concurrency by enabling multiple threads to operate on different segments of the map simultaneously.
    2. **Atomic Operations**: **`ConcurrentHashMap`** offers atomic operations such as **`putIfAbsent`**, **`replace`**, and **`remove`**. These methods help in performing common compound actions (like check-then-act) safely without external synchronization.
    3. **When to Use `ReentrantLock`**: You might consider using **`ReentrantLock`** or other explicit locking mechanisms in scenarios where you need to perform multiple operations on the map as a single atomic unit. For example, if you have a sequence of operations that need to be executed together without interference from other threads, then explicit locking might be necessary.
    4. **Lock Granularity**: **`ReentrantLock`** locks at the object level, meaning that when a thread holds the lock, no other thread can access any part of the locked object. In contrast, **`ConcurrentHashMap`** locks at a finer granularity (segment or bucket level), allowing higher concurrency.
    5. **Simplicity and Performance**: Using **`ConcurrentHashMap`** without external locks is simpler and often offers better performance due to its fine-grained locking mechanism. Adding explicit locks can make the code more complex and might degrade performance if not used judiciously.
  - **Used:** ConcurrentHashMap because of its simplicity and performance benefits.

- Choice between ArrayList, ArrayOnCopyList, ConcurrentSkipListSet or Collections.synchronizedList to store Quotes data in sorted order by their timestamp of ingestion?
  - ArrayList: Fast iteration, but poor concurrency; manual synchronization needed; not ideal for frequent updates.
  - CopyOnWriteArrayList: Thread-safe, good for read-heavy but infrequent updates; expensive on writes.
  - ConcurrentSkipListSet: Concurrent, sorted, good for frequent updates; slightly slower iteration.
  - Collections.synchronizedList: Basic synchronization; requires external synchronization for iteration; moderate performance.
  - **Used:** ConcurrentSkipListSet and Sorted the list entries based on timestamp
- Choice of having Executor Service Singleton Bean vs Having Executor Service for scheduling the cleanup of each datastore?
  - Executor Service Singleton Bean: 
    - Pros:
      - Centralized management, resource-efficient, easier to monitor and control. 
    - Cons:
      - Less flexible for datastore-specific policies.
  - Individual Executor Service for Each Datastore: 
    - Pros: 
      - Greater flexibility, tailored scheduling per datastore. 
    - Cons: More resource-intensive, complex management.
  -**Used:** Executor Service as Singleton Bean because of resource efficiency and also we don't need datastore specific policies
