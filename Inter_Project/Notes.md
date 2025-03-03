# ItemReader

1. JdbcCursorItemReader/JpaCursorItemReader (Reader reads only one item and gives to Processor)
    1. RowMapper(row,rowNum) (Much like line mapper, gives one row)
    2. (String) sql
2. JdbcPagingItemReader/JpaPagingItemReader (Reader reads a page and gives items to Processor one by one)
    1. RowMapper
    2. QueryProvider
3. StoredProcedureItemReader

# Technique

***There are some cases when database can be updated while running batch. In that case we can miss any column or
reprocess any column.***
1. `Stateless Database Reading & JdbcCursorItemReader` (In this we need to add additional column called `processed`.When
   any column is changed processed column is set to false.)
    1. processed column
    2. saveState(false)
    3. allowStartIdComplete(true) on JobBuilder
2. `Driving Query & JdbcPagingItemReader`

# ItemWriter
1. JdbcBatchItemWriter
   1. itemPreparedStatement
   2. sql
2. JpaItemWriter