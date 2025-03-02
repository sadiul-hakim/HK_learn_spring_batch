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
.allowStartIfComplete() on StepBuilder.***
***By default, in case of failure, Step starts from the same point where it was failed. But if the execution is needed
to
be started from the beginning we can set `.saveState(false)` on ItemReader.***