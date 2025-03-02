1. ItemReader
    1. FlatFileItemReader reads from file line by line
        1. Line Tokeniser
            1. Delimited Line Tokeniser
            2. FixedLength Line Tokeniser
        2. Line Mapper
    2. StaxEventReader (To Read xml)
        1. Unmarshaller
2. ItemWriter
    1. StaxEventWriter
        1. Marshaller
    2. FlatFileItemWriter

`Job consists of Multiple Steps and Step consists of Reader,Processor(Optional),Writer`