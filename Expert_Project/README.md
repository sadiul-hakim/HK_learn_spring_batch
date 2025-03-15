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

# `Batch Processing Scalability`

# `1. Running Step through multiple thread` ⭕

We can run Step through multiple step using `.taskExecutor()` method on StepBuilder. But we have to consider few things
like all threads might do the same thing. Suppose: we have 3 threads all of them started reading from 0th index. That is
what we do not want. We want if thread 1 starts from 0 thread 2 should start from 50 and then thread 3 should start
from 100. Somehow these threads should coordinate, or they should know their task. To solve this problem we can
synchronise and share same Reader and writer with multiple threads and send them some synchronisation data.

To synchronise Reader we can use SynchronisedItemStreamReaderBuilder and delegate the ItemReader to it.
interface ItemStreamReader<T> extends ItemStream, ItemReader<T> and almost all the Item Reads implements
ItemStreamReader. So, we can return ItemReader or ItemStreamReader.

***Running Steps though multiple threads comes with more issues like thread deadlock, we need to synchronise the writer.
The output could be wrong and that has nothing to do with Spring Batch it is on CPU we don't know the execution
order. We have better solution for this.***

---

# `2. Remote Chunking`  ⭕

***In this approach we use multiple machine for data processing. Main machine Reads and Writes data and other multiple
machines process data. They use Queue(Kafka,RabbitMQ) for communication. `This way is out of my learning scope`.***

## Pros and Cons

### Pros

1. Real Parallel Processing

### Cons

1. Overhead
    1. System Complexity
    2. Network Traffic
    3. Duplicate Reading
    4. Inducing Queue
2. Same Code base used
3. Tight Coupling with Spring Integration

`Remote Chunking should not be used when we have a lots of data in our data Source as all the data would be sent to all
worker mechine by queue. That is an overhead.`

# `Partitioning` ✔️

## Partitioner Interface

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

## Partition Step

### Overview

A **Partition Step** in Spring Batch is a special step that uses a `Partitioner` to split the workload across multiple
**worker steps**, enabling parallel execution. This enhances performance, especially for large-scale data processing
tasks.

### Responsibilities

- Dividing the main step into smaller steps (partitions).
- Distributing partitions across worker steps for parallel execution.
- Coordinating the execution of worker steps.

### Behind the Scenes

The **Partition Step** involves multiple internal components that work together to enable partitioned execution:

1. **Splitter**: Uses the `Partitioner` implementation to divide the workload into smaller partitions.
2. **Handler**: Assigns each partition to an available worker step.
3. **Task Executor**: Manages concurrent execution of worker steps.
4. **Aggregator**: Collects results from all worker steps and determines the final execution status.

### Configuration

To configure a **Partition Step**, define the **partitioned step** with a `Partitioner`:

```java

@Bean
public Step workerStep() {
    return stepBuilderFactory.get("workerStep")
            .<String, String>chunk(10)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}

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

### Key Components of a Partition Step

1. **Partitioner**: Splits the data into multiple partitions.
2. **Worker Step**: The step that processes each partition.
3. **Partitioned Step**: The step that orchestrates partitioning and worker step execution.
4. **Task Executor**: Ensures parallel execution of worker steps.

`What Partioner Step do is, It creates multiple(num of gridSize) ExecutionContext then assing those to same provided
 step ony by one and run them parallely.`

## Worker Step

### Overview

A **Worker Step** in Spring Batch is the step that processes a single partition of data. Each partition is executed
independently, allowing parallelism in batch processing.

### Responsibilities

- Processing the assigned partition of data.
- Handling chunk-based or item-based processing.
- Reporting execution status back to the **Partition Step**.

### Configuration

To define a **Worker Step**, configure a step with a reader, processor, and writer:

```java

@Bean
public Step workerStep() {
    return stepBuilderFactory.get("workerStep")
            .<String, String>chunk(10)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}
```

### Key Components of a Worker Step

1. **ItemReader**: Reads data for the assigned partition.
2. **ItemProcessor**: Transforms or processes the data.
3. **ItemWriter**: Writes the processed data to the target destination.

## Flow of Execution

When using **Partition Step, Partitioner, and Worker Steps**, the execution follows this sequence:

1. **Job Starts**: A Spring Batch job is triggered.
2. **Partition Step Execution**: The **Partition Step** starts execution.
3. **Partitioning the Data**: The configured **Partitioner** divides the dataset into multiple partitions based on a
   given grid size.
4. **Assigning Partitions to Worker Steps**: The `ExecutionContext` for each partition is assigned to a separate *
   *Worker Step**.
5. **Parallel Execution of Worker Steps**: Each **Worker Step** processes its assigned partition in parallel using a *
   *task executor**.
6. **Completion and Aggregation**: After all **Worker Steps** complete execution, the **Partition Step** aggregates the
   results.
7. **Job Completion**: The job execution status is updated in the **JobRepository**, marking it as complete or failed if
   any worker step encounters an issue.

`Flow is used to run multiple steps in parallel but Pertition Step is used to pertition single step.`