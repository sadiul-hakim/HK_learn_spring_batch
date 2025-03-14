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

# Running Step through multiple thread

We can run Step through multiple step using `.taskExecutor()` method on StepBuilder. But we have to consider few things
like all threads might do the same thing. Suppose: we have 3 threads all of them started reading from 0th index. That is
what we do not want. We want if thread 1 starts from 0 thread 2 should start from 50 and then thread 3 should start
from 100. Somehow these threads should coordinate or they should know their task.

---

# Partitioner Interface

### Overview

`Partitioner` is an interface in Spring Batch that enables parallel processing by splitting a dataset into multiple
partitions. Each partition is then processed by an individual step execution, allowing better scalability and
performance.

### Responsibilities

- Dividing a dataset into multiple partitions.
- Assigning each partition to a worker step for parallel execution.
- Ensuring load balancing among the worker steps.

### Implementation Example

Below is a simple implementation of the `Partitioner` interface:

```java
public class CustomPartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("partitionIndex", i);
            partitions.put("partition" + i, context);
        }
        return partitions;
    }
}
```

### Configuration in a Step

To use the partitioner, define a **partitioned step**:

```java

@Bean
public Step partitionedStep(Step workerStep) {
    return stepBuilderFactory.get("partitionedStep")
            .partitioner("workerStep", new CustomPartitioner())
            .step(workerStep)
            .gridSize(4) // Number of partitions
            .taskExecutor(new SimpleAsyncTaskExecutor())
            .build();
}
```

## Differences Between JobRepository, JobExplorer, and Partitioner

| Feature              | JobRepository | JobExplorer | Partitioner |
|----------------------|---------------|-------------|-------------|
| Read-Write           | Yes           | No          | No          |
| Tracks Job Execution | Yes           | Yes         | No          |
| Manages Restarts     | Yes           | No          | No          |
| Query Job History    | No            | Yes         | No          |
| Parallel Processing  | No            | No          | Yes         |

## Conclusion

- Use `JobRepository` when you need to **persist and manage job executions**.
- Use `JobExplorer` when you need to **query job history and execution status** without modifying the data.
- Use `Partitioner` when you need to **split and distribute workload** across multiple processing units for parallel
  execution.
- All three components are essential for building scalable and maintainable Spring Batch applications.
