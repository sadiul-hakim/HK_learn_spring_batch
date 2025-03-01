# Spring Data JDBC

## 1. What is Spring Data JDBC?
Spring Data JDBC is a lightweight, opinionated framework that simplifies database interactions using JDBC (Java Database Connectivity). It provides an alternative to Spring Data JPA by offering a simple approach to data persistence without using an ORM (Object-Relational Mapping) framework like Hibernate.

Spring Data JDBC follows a repository-based approach and uses direct SQL operations while keeping the domain model as the primary abstraction.

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
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
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
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
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
Spring Data JDBC provides a simple and efficient way to work with relational databases without the complexity of an ORM. It is a great choice for applications that require direct JDBC interaction while leveraging Spring's repository abstraction.

# DataSource Class in Java

## Introduction
The `DataSource` interface in Java is a part of the `javax.sql` package and provides a standard way to connect to relational databases. It is an alternative to the traditional `DriverManager` for managing database connections in Java applications.

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
The `DataSource` interface is a powerful and flexible way to manage database connections in Java applications. It enhances performance, security, and scalability by supporting connection pooling and transaction management. Spring Boot makes configuring and using `DataSource` easy with minimal boilerplate code.

# DataSource Class in Java

## Introduction
The `DataSource` interface in Java is a part of the `javax.sql` package and provides a standard way to connect to relational databases. It is an alternative to the traditional `DriverManager` for managing database connections in Java applications.

## Why Use DataSource?
Using `DataSource` is preferred over `DriverManager` because:
- **Connection Pooling**: It supports connection pooling, improving performance.
- **Managed Transactions**: Works well with JTA (Java Transaction API) for better transaction management.
- **Easier Configuration**: Can be configured externally (e.g., via Spring Boot or application servers).
- **Security**: Allows better control over credentials and connection properties.

## Does Spring Data JDBC Provide DataSource?
Yes, Spring Boot and Spring Data JDBC provide a `DataSource` by default. When using `spring-boot-starter-data-jdbc`, Spring Boot automatically configures a `DataSource` based on the properties defined in `application.properties` or `application.yml`. It typically defaults to **HikariCP**, a high-performance connection pool.

## Implementations of DataSource
Several implementations of `DataSource` exist, including:
- **HikariCP (Default in Spring Boot)**: A high-performance connection pool (`HikariDataSource`).
- **Basic DataSource**: Provided by Apache Commons DBCP (`BasicDataSource`).
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
### Dependencies (Maven)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
```

### Application Properties
Spring Boot auto-configures the `DataSource` when these properties are set:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.maximum-pool-size=10
```

### Java Configuration (Optional)
If you want to explicitly define a `DataSource`, you can do it like this:
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
The `DataSource` interface is a powerful and flexible way to manage database connections in Java applications. Spring Boot, through `spring-boot-starter-data-jdbc`, provides a `DataSource` automatically using HikariCP. This makes it easy to work with databases without additional configuration unless customization is needed.

