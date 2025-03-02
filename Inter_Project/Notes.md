***Reader Reads item one by one, Processor processes item one by one but writer writes chunk of items.***

# JobInstance

***JobInstance is an instance of Job.JobInstance is identified uniquely by Job and Job Parameters.
When we try to run job with same parameters same JobInstance is reused.***

***If same JobInstance is reused Spring Batch by default does not execute successfully executed Steps.***

# JobExecution & StepExecution

***JobExecution & StepExecution is created newly whenever a job is executed. They are not reused.***