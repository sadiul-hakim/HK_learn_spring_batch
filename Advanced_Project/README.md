`When we run our app as a server we should add @EnableBatchProcessing`

# JobLauncher & TaskExecutorJobLauncher & SimpleAsyncTaskExecutor

***JobLauncher is used to launch Job programmatically. SimpleAsyncTaskExecutor is used while building a
TaskExecutorJobLauncher
that is capable of running jobs asynchronously. we can set .setVirtualThread() to true to ask SimpleAsyncTaskExecutor to
use Virtual Thread.***

# Tasklet

All Steps do not need a Reader, Processor and Writer. Using Tasklet we can write plain logics without
them(R,P,W).

This is a FunctionalInterface, it has .execute() that returns RepeatStatus. RepeatStatus has two values CONTINUABLE and
FINISHED. If CONTINUABLE is returned the .execute() method is called again and again until FINISHED is returned.

Tasklet itself is not a Step it needs to be passed in a Step to be run.

# ResourceAwareItemReaderItemStream & FlatFileItemReader & MultiResourceItemReaderBuilder

1. ResourceAwareItemReaderItemStream is used to read from multiple resource like multi-file.
   ResourceAwareItemReaderItemStream takes another Reader(FlatFileItemReader) to read from resources.
   ResourceAwareItemReaderItemStream sets the resource to the other Reader(FlatFileItemReader) one by one.
   It has 3 important methods .open(ExecutionContext) where we open the other reader .close() where we close the other
   Reader,
   and .read() where we read using the other Reader(FlatFileItemReader) and return a Object.
   `ResourceAwareItemReaderItemStream only manages the resource. It does not read itself.`

2. MultiResourceItemReaderBuilder then takes ResourceAwareItemReaderItemStream and manages the whole process.

Or for clearance,

# Understanding MultiFileTeamReader and FlatFileItemReader

## Overview

### `MultiResourceItemReader<Team>`

- Handles reading from multiple files.
- Receives a list of files (`resources(inputFolderPath)`).
- Delegates file reading to `MultiFileTeamReader`.

### `MultiFileTeamReader` (Implements `ResourceAwareItemReaderItemStream<Team>`)

- Acts as an intermediate layer.
- Receives one file at a time from `MultiResourceItemReader`.
- Assigns the file as a resource to `FlatFileItemReader<String>`.
- Keeps track of which file is currently being processed.

### `FlatFileItemReader<String>`

- Reads lines from the assigned file.
- Keeps track of the current line and moves sequentially.
- Needs a proper `lineMapper` to parse each line correctly.

## Flow Summary

1. `MultiResourceItemReader` manages multiple files.
2. `MultiFileTeamReader` keeps track of the current file and sets it in `FlatFileItemReader`.
3. `FlatFileItemReader` reads the lines within the assigned file.

`FlatFileItemReader remembers which line it read last and where to start next. It maintains an internal state that 
allows it to continue reading sequentially from where it left off.`

## Handling Line Separation

- Teams in the input files are **separated by empty lines**.
- `FlatFileItemReader` reads each team and keeps track of where it stopped.
- When encountering an **empty line**, `read()` should:
    1. Detect the empty line.
    2. Create a new `Team` object.
    3. Return the `Team`, allowing `FlatFileItemReader` to continue reading.

## Behavior of `FlatFileItemReader`

✅ **Does `FlatFileItemReader` remember where it stopped?**

- Yes, it keeps track of the last read position and resumes from the next unread line.

✅ **Does `FlatFileItemReader` hold the file resource until it’s finished?**

- Yes, it keeps the file open and reads sequentially until EOF.
- Once done, it releases the resource and moves to the next file assigned by `MultiResourceItemReader`.

`Most Readers by default save its state in ExecutionContext.`

### What is `ItemStream`?

- `ItemStream` is an interface in Spring Batch that provides lifecycle management for components that need to store and
  retrieve execution state.
- It is primarily used in readers, writers, and processors that need to maintain information across step executions.

### Key Methods in `ItemStream`

1. **`open(ExecutionContext executionContext)`**
    - Called at the beginning of a step.
    - Used to initialize resources and restore previous execution state.

2. **`update(ExecutionContext executionContext)`**
    - Called periodically during step execution.
    - Saves current state (e.g., last read item index) to allow restartability.

3. **`close()`**
    - Called at the end of a step.
    - Used to release resources (files, connections, etc.).

### How `ItemStream` Works in Our Case

- `FlatFileItemReader` implements `ItemStream`, so it:
    - Saves the last read line position using `update()`.
    - Can restart from the last position if the job is restarted.
    - Releases file resources using `close()` after processing.
- `MultiFileTeamReader` also implements `ItemStream`, ensuring proper file handling across multiple files.

# Accessing JobParameters

***we can access JobParameters like this : `@Value("#{jobParameters['scoreIndex']}")`. When we access
JobParameters,jobExecutionContext like
this
we need to annotate the method with `@StepScope`. @StepScope annotated @Bean only lives in scope of a step. But the
whole
Step can not be defined as @StepScope only beans used in a Step can be @StepScope. Because, In order to create a @Bean
@StepScope we need an active StepExecution.***

# Sharing Data from one step to others

***To share data across Steps we can transfer keys/values we are interested in to Job ExecutionContext at the end of
Step
Execution. We can use StepExecution.getJobExecution().getExecutionContext() to get ExecutionContext of Job. But we have
another way `ExecutionContextPromotionListener`. We need to pass an Array of interested keys to .setKeys() of
ExecutionContextPromotionListener to promote those keys to Job ExecutionContext from Step ExecutionContext.***

# Accessing shared ExecutionContext Data

***We can use @Value("#jobExecutionContext['max.score']") to access Job ExecutionContext. ***

# Appending Some Text at the Top/Bottom of Output File

***`FlatFileHeaderCallback` is used to append some text at the top of output file before writer writes anything.***
***`FlatFileFooterCallback` does the opposite.***

# Executing Steps in parallel

***To execute steps in parallel we need to make Flow of Steps and pass TaskExecutor.***

# CommandRunner & JvmCommandRunner

JvmCommandRunner is provided by Spring Batch to execute some system command for Batch Applications.