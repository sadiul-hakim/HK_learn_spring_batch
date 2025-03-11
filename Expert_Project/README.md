# Spring Batch: JobRepository and JobExplorer

Spring Batch provides a robust framework for batch processing. Two essential components for managing job executions and
retrieving job-related data are **JobRepository** and **JobExplorer**.

## JobRepository

### Overview

`JobRepository` is the central component in Spring Batch responsible for persisting batch job metadata. It stores
details about job executions, step executions, and job instances in a database.

### Responsibilities

- Persisting job and step execution metadata.
- Tracking job execution status and history.
- Storing job parameters and context.
- Managing job restarts and recoveries.

### Configuration

To configure `JobRepository`, you need a **DataSource** and a **BatchConfigurer**:

```java

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager)
            throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setDatabaseType("POSTGRES"); // Change according to your database
        return factory.getObject();
    }
}
```

## JobExplorer

### Overview

`JobExplorer` is a read-only version of `JobRepository`, allowing access to job execution data without modifying it. It
is useful for querying batch job history and monitoring running jobs.

### Responsibilities

- Fetching job instances and job executions.
- Querying step executions.
- Retrieving execution parameters.

### Configuration

To configure `JobExplorer`, you can use the `JobExplorerFactoryBean`:

```java

@Bean
public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
    JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
    factory.setDataSource(dataSource);
    return factory.getObject();
}
```

### Usage Example

You can use `JobExplorer` to fetch job execution details:

```java

@Autowired
private JobExplorer jobExplorer;

public void getJobDetails() {
    List<JobInstance> jobInstances = jobExplorer.getJobInstances("myJob", 0, 10);
    for (JobInstance instance : jobInstances) {
        List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
        executions.forEach(execution -> System.out.println("Job Execution ID: " + execution.getId()));
    }
}
```

## Differences Between JobRepository and JobExplorer

| Feature              | JobRepository | JobExplorer |
|----------------------|---------------|-------------|
| Read-Write           | Yes           | No          |
| Tracks Job Execution | Yes           | Yes         |
| Manages Restarts     | Yes           | No          |
| Query Job History    | No            | Yes         |

## Conclusion

- Use `JobRepository` when you need to **persist and manage job executions**.
- Use `JobExplorer` when you need to **query job history and execution status** without modifying the data.
- Both are crucial for building scalable and maintainable Spring Batch applications.

