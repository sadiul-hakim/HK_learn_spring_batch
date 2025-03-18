***Reader Reads item one by one, Processor processes item one by one but writer writes chunk of items.***

# JobInstance

***JobInstance is an instance of Job.JobInstance is identified uniquely by Job and Job Parameters.
When we try to run job with same parameters same JobInstance is reused.***

***If same JobInstance is reused Spring Batch by default does not execute successfully executed Steps.***

# JobExecution & StepExecution

***JobExecution & StepExecution is created newly whenever a job is executed. They are not reused.***

***JobExecution & StepExecution have Execution Context.***

# ExecutionContext

***Spring Batch has Job Scoped and Step Scoped ExecutionContext. They are like Collection of key/value pairs that are
persisted and controlled by Spring Batch.***

***For Job ExecutionContext, ExecutionContext is persisted in database after each step is completed. For Step
ExecutionContext,
ExecutionContext is persisted after each chuck is commited(Item Writer writes each chuck). ExecutionContext is persisted
no matter if there is a exception. ExecutionContext is reused for the same JobInstance.***

# Restarting a Job

***Every job has a exitStatus***

***Successfully Completed Jobs are not restartable. e.g: Suppose a job has some job parameters and last time it was
finished
successfully, if we try to restart the job with same parameters (meaning same job instance would be used) batch would
throw
exception.
`But in case of empty parameters it is different. With enpty parameters successfully completed jobs can be 
restarted.`***

***You can use .preventRestart() on JobBuilder class to prevent jobs from restarting.***

---

***Now job is restarted***

# Restarting a Step

***Successfully completed steps are skipped, but they do not throw exception. But the behaviour can be changed by
`.allowStartIfComplete()` on StepBuilder. When a step is restarted(in case of allowStartIfComplete() or execution
failure),
we can set the limit how many times the step should be restarted/executed set using `startLimit(int)` on StepBuilder.
If the limit is exceeded it would throw exception.***

***By default, in case of failure, Step starts from the same point where it was failed. But if the execution is needed
to
be started from the beginning we can set `.saveState(false)` on ItemReader.***

# Life Cycle

***Spring Batch gives us functionality to do something before/after job execution or before/after step execution.
We can use `JobExecutionListener,StepExecutionListener and .listener()` on JobBuilder/StepBuilder to do these kind of
things.
Also, There are Annotations like `@BeforeJob/@BeforeStep And @AfterJob/@AfterStep`***

# Why Separating the ItemReader into a `@Bean` Works

You've hit upon a common Spring Batch behavior related to how dependency injection and bean lifecycle management work within the framework. Here’s why extracting your `ItemReader` into a separate `@Bean` likely resolved your `NullPointerException`:

## 1. Bean Lifecycle and Initialization Order

- **Step-Scoped Beans:** When you define your `ItemReader` directly within the step definition (e.g., as part of an anonymous inner class or a lambda expression), Spring Batch might not initialize the reader before it’s used. This can lead to situations where dependencies like your `DataSource` haven't been properly injected.

- **Application Context Beans:** By extracting the `ItemReader` into a separate `@Bean` within a `@Configuration` class, you’re placing it under the control of the application context. Spring's application context ensures that all dependencies are injected and the bean is fully initialized before it's made available for use. Spring initializes the beans in the application context before the steps are executed. This guarantees that the datasource will be available when the reader is created.

## 2. Dependency Injection Timing

- **Step Configuration:** The way Spring Batch configures steps might not always guarantee immediate dependency injection for inline-defined components.

- **Bean Definition:** A dedicated `@Bean` definition explicitly tells Spring to manage the creation and injection of the `ItemReader`, ensuring that the `DataSource` is injected at the appropriate time.

## 3. Scope and Proxying

- **Spring Batch uses proxies and scopes** to manage the lifecycle of components within a step. This can sometimes introduce complexities in dependency injection, especially when dealing with inline definitions.

- **Defining the reader as a Bean** removes some of the complexity that the step scope introduces.

## In essence:

- Moving the `ItemReader` to a separate `@Bean` promotes better control over its lifecycle and dependency injection.
- It forces Spring to fully initialize the reader before it’s used within the step, preventing the `NullPointerException` caused by a missing `DataSource`.
- It also improves code readability and maintainability.

## Best Practices:

- It’s generally recommended to define your `ItemReader`, `ItemWriter`, and `ItemProcessor` components as **separate `@Bean`s** whenever possible.
- This promotes cleaner code, better testability, and reduces the likelihood of dependency injection issues.
- It also allows for easier reuse of these components across multiple steps or jobs.

## Key Points

- The most common cause of this error is a **null `DataSource`**.
- Carefully review your Spring configuration and database connection details.
- Add null checks to your datasource.
