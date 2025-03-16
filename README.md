# 1. Spring Data JDBC

## 1. What is Spring Data JDBC?

Spring Data JDBC is a lightweight, opinionated framework that simplifies database interactions using JDBC (Java Database
Connectivity). It provides an alternative to Spring Data JPA by offering a simple approach to data persistence without
using an ORM (Object-Relational Mapping) framework like Hibernate.

Spring Data JDBC follows a repository-based approach and uses direct SQL operations while keeping the domain model as
the primary abstraction.

## 2. What Does It Offer?

Spring Data JDBC offers:

- Simplicity: No complex ORM features, focusing on direct SQL execution.
- Repository Support: Built-in repository interfaces to simplify CRUD operations.
- Immutable Entities: Encourages the use of immutable domain models.
- Event Handling: Provides hooks for lifecycle events like `BeforeSave` and `AfterLoad`.
- Transparent Mapping: Uses `@Table` and `@Id` for defining database relationships.

## 3. Elements and Entities

The key elements in Spring Data JDBC are:

- **Repository Interface**: Defines methods for performing CRUD operations.
- **Domain Entity**: A simple Java object annotated with `@Table` representing a database table.
- **Id Annotation (`@Id`)**: Specifies the primary key field.
- **Repositories (`CrudRepository`, `PagingAndSortingRepository`)**: Predefined interfaces for data access.
- **Aggregate Root**: Represents the main entity that owns relationships with other entities.

## 4. Available Classes for Developers

Spring Data JDBC provides the following key classes and interfaces:

- `CrudRepository<T, ID>` – Basic CRUD operations.
- `PagingAndSortingRepository<T, ID>` – CRUD operations with pagination and sorting.
- `JdbcAggregateTemplate` – A lower-level abstraction for executing queries manually.
- `@MappedCollection` – Helps in managing one-to-many relationships.
- `@Query` – Allows writing custom SQL queries.

## 5. How to Configure Spring Data JDBC?

### Dependencies (Maven)

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Application Properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/todo_db
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jdbc.template.query-timeout=30
```

### Enable JDBC Repository

```java

@Configuration
@EnableJdbcRepositories
public class JdbcConfig {
}
```

## 6. Todo CRUD Example

### Entity Class

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("todos")
public class Todo {
    @Id
    private Long id;
    private String title;
    private boolean completed;

    // Constructor
    public Todo(Long id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }
}
```

### Repository Interface

```java
import org.springframework.data.repository.CrudRepository;

public interface TodoRepository extends CrudRepository<Todo, Long> {
}
```

### Service Layer

```java
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TodoService {
    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public Iterable<Todo> findAll() {
        return repository.findAll();
    }

    public Optional<Todo> findById(Long id) {
        return repository.findById(id);
    }

    public Todo save(Todo todo) {
        return repository.save(todo);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
```

### Controller Layer

```java
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public Iterable<Todo> getTodos() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Todo> getTodoById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        return service.save(todo);
    }

    @DeleteMapping("/{id}")
    public void deleteTodoById(@PathVariable Long id) {
        service.deleteById(id);
    }
}
```

### Running the Application

- Run the application using `SpringBootApplication`.
- Test endpoints using Postman or CURL:
    - `GET /todos` – Fetch all todos
    - `GET /todos/{id}` – Fetch a single todo
    - `POST /todos` – Create a new todo
    - `DELETE /todos/{id}` – Delete a todo

## Conclusion

Spring Data JDBC provides a simple and efficient way to work with relational databases without the complexity of an ORM.
It is a great choice for applications that require direct JDBC interaction while leveraging Spring's repository
abstraction.

---

# 2. DataSource Class in Java

## Introduction

The `DataSource` interface in Java is a part of the `javax.sql` package and provides a standard way to connect to
relational databases. It is an alternative to the traditional `DriverManager` for managing database connections in Java
applications.

## Why Use DataSource?

Using `DataSource` is preferred over `DriverManager` because:

- **Connection Pooling**: It supports connection pooling, improving performance.
- **Managed Transactions**: Works well with JTA (Java Transaction API) for better transaction management.
- **Easier Configuration**: Can be configured externally (e.g., via Spring Boot or application servers).
- **Security**: Allows better control over credentials and connection properties.

## Implementations of DataSource

Several implementations of `DataSource` exist, including:

- **Basic DataSource**: Provided by Apache Commons DBCP (`BasicDataSource`).
- **HikariCP**: A high-performance connection pool (`HikariDataSource`).
- **Tomcat JDBC Pool**: A lightweight alternative (`org.apache.tomcat.jdbc.pool.DataSource`).
- **C3P0**: Another widely used connection pooling library.

## Key Methods in DataSource

The `DataSource` interface provides three main methods:

```java
public interface DataSource {
    Connection getConnection() throws SQLException;

    Connection getConnection(String username, String password) throws SQLException;

    PrintWriter getLogWriter() throws SQLException;

    void setLogWriter(PrintWriter out) throws SQLException;

    void setLoginTimeout(int seconds) throws SQLException;

    int getLoginTimeout() throws SQLException;

    Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
```

## Configuring DataSource in Spring Boot

### Maven Dependencies

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
<groupId>com.zaxxer</groupId>
<artifactId>HikariCP</artifactId>
</dependency>
```

### Application Properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.maximum-pool-size=10
```

### Java Configuration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("root");
        dataSource.setPassword("password");
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }
}
```

## Using DataSource in a Repository

```java
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int countUsers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
    }
}
```

## Conclusion

The `DataSource` interface is a powerful and flexible way to manage database connections in Java applications. It
enhances performance, security, and scalability by supporting connection pooling and transaction management. Spring Boot
makes configuring and using `DataSource` easy with minimal boilerplate code.

---

# 3. Spring Batch Overview

Spring Batch is a framework designed for processing large volumes of data in batch jobs. It provides robust support for
transaction management, job processing, and parallel execution while ensuring fault tolerance, scalability, and
performance.

## Key Features of Spring Batch

- **Chunk-based processing:** Reads large data sets in chunks for efficient processing.
- **Transaction management:** Supports rollback and retry mechanisms.
- **Job scheduling & execution:** Jobs can be triggered manually, scheduled, or event-driven.
- **Parallel processing & scalability:** Supports multi-threading and partitioning for high performance.
- **Built-in readers & writers:** Provides various data sources such as databases, files, and messaging systems.
- **Error handling & retries:** Built-in mechanisms for skipping and retrying failed records.

---

## 1. Dependency for Spring Batch

To use Spring Batch, add the following dependencies to your `pom.xml` (for Maven-based projects):

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

For **Gradle**:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-batch'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'org.hsqldb:hsqldb'
}
```

---

## 2. Key Elements of Spring Batch

Spring Batch is built around several core concepts:

### i. Job

A **Job** is the main container for batch processing. It consists of **one or more Steps**.

```java

@Bean
public Job myJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("myJob", jobRepository)
            .start(step1)
            .build();
}
```

### ii. Step

A **Step** defines a stage in the job, such as reading, processing, and writing data.

```java

@Bean
public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("step1", jobRepository)
            .<String, String>chunk(10, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}
```

### iii. ItemReader

Reads data from a source (e.g., database, file, or API).

```java

@Bean
public ItemReader<String> itemReader() {
    return new ListItemReader<>(List.of("item1", "item2", "item3"));
}
```

### iv. ItemProcessor

Processes each item before writing.

```java

@Bean
public ItemProcessor<String, String> itemProcessor() {
    return item -> item.toUpperCase(); // Convert to uppercase
}
```

### v. ItemWriter

Writes the processed data to an output source.

```java

@Bean
public ItemWriter<String> itemWriter() {
    return items -> items.forEach(System.out::println);
}
```

---

## 3. How Spring Batch Works

Spring Batch follows a structured flow:

1. **JobLauncher** starts a Job.
2. **Job** contains multiple Steps.
3. Each **Step** processes data in a sequence:

- **ItemReader** reads data.
- **ItemProcessor** transforms data.
- **ItemWriter** writes data.

4. **Job Repository** stores job execution details.
5. Job completes, fails, or restarts as needed.

---

## 4. Basic Example of a Spring Batch Job

```java

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job myJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("myJob", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new ListItemReader<>(List.of("Java", "Spring", "Batch"));
    }

    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return item -> "Processed: " + item;
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> items.forEach(System.out::println);
    }
}
```

### Expected Output

```
Processed: Java
Processed: Spring
Processed: Batch
```

---

## Conclusion

Spring Batch is a powerful framework for handling batch processing tasks efficiently. By defining **Jobs, Steps,
ItemReaders, Processors, and Writers**, you can create scalable and robust data processing pipelines. 🚀

---

# 4. Spring Data JDBC and Its Relationship with Spring Batch

## Introduction

Spring Data JDBC is a lightweight, opinionated framework for interacting with relational databases using plain JDBC
while leveraging Spring’s conventions. It provides a simplified alternative to Spring Data JPA by focusing on direct SQL
execution without an ORM like Hibernate.

Spring Batch is a framework designed for batch processing, enabling high-volume data operations such as ETL (Extract,
Transform, Load), data migration, and scheduled job execution.

Spring Data JDBC and Spring Batch can work together to manage batch processing efficiently, leveraging direct database
access without the overhead of an ORM.

## Key Features of Spring Data JDBC

- **Simpler than JPA**: Avoids complexity by eliminating lazy loading and dirty checking.
- **Direct SQL Execution**: Uses repositories and SQL queries to interact with the database.
- **Lightweight Data Mapping**: Maps entities to database rows with minimal overhead.
- **Better Performance for Bulk Operations**: Since it directly interacts with JDBC, it provides better performance in
  batch processing scenarios.

## How Spring Data JDBC Integrates with Spring Batch

Spring Batch requires efficient database interactions, and Spring Data JDBC can be used in various components of batch
processing:

### 1. **Item Reader with Spring Data JDBC**

Spring Batch uses `ItemReader` to read data in chunks. Spring Data JDBC repositories can serve as a simple way to fetch
data.

#### Example: Using `JdbcCursorItemReader` with Spring Data JDBC

```java

@Bean
public JdbcCursorItemReader<MyEntity> itemReader(DataSource dataSource) {
    return new JdbcCursorItemReaderBuilder<MyEntity>()
            .name("jdbcCursorItemReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM my_table")
            .rowMapper(new BeanPropertyRowMapper<>(MyEntity.class))
            .build();
}
```

### 2. **Item Writer with Spring Data JDBC**

Spring Batch writes processed data using `ItemWriter`. Spring Data JDBC repositories can be injected to persist data
efficiently.

#### Example: Using `JdbcBatchItemWriter`

```java

@Bean
public JdbcBatchItemWriter<MyEntity> itemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<MyEntity>()
            .dataSource(dataSource)
            .sql("INSERT INTO processed_table (id, name) VALUES (:id, :name)")
            .beanMapped()
            .build();
}
```

### 3. **Transaction Management**

Spring Data JDBC supports declarative transactions using `@Transactional`, which helps ensure consistency in batch jobs.
Spring Batch also provides transactional behavior for each step.

### 4. **Job Repository with Spring Data JDBC**

Spring Batch requires a `JobRepository` to store metadata about batch jobs. Spring Boot auto-configures this using a
database, and Spring Data JDBC can be leveraged to manage batch-related data.

## Example: Batch Processing with Spring Data JDBC

```java

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<MyEntity> reader,
                     ItemProcessor<MyEntity, MyEntity> processor, ItemWriter<MyEntity> writer) {
        return stepBuilderFactory.get("step")
                .<MyEntity, MyEntity>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }
}
```

## Conclusion

Spring Data JDBC simplifies database interactions and integrates well with Spring Batch for high-performance batch
processing. By leveraging direct SQL execution, efficient item readers/writers, and transaction management, Spring Data
JDBC provides an optimized approach for batch job execution without the complexity of an ORM.


