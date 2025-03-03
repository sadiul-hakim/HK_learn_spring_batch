# ItemReader

1. JdbcCursorItemReader/JpaCursorItemReader (Reader reads only one item and gives to Processor)
   1. RowMapper(row,rowNum) (Much like line mapper, gives one row)
   2. (String) sql
2. JdbcPagingItemReader/JpaPagingItemReader (Reader reads a page and gives items to Processor one by one)
   1. RowMapper
   2. QueryProvider
3. StoredProcedureItemReader

# Technique

1. Stateless Database Reading (In this technique Spring Batch adds additional column called `processed`. There are some
   cases when database can be updated while running batch
   . In that case we can miss any column or reprocess any column.)
    1. processed column is handled by Batch
    2. saveState(false)