package xyz.sadiulhakim.inter_project.pojo;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;

public record BankTransaction(long id, int month, int day, int hour, int minute, BigDecimal amount, String merchant) {

    // Query and row mapper for obtaining bank transactions from the database
    public static final String SELECT_ALL_QUERY = "select id, month, day, hour, minute, amount, merchant from bank_transaction_yearly";
    public static final RowMapper<BankTransaction> ROW_MAPPER = (rs, rowNum) -> new BankTransaction(
            rs.getLong("id"),
            rs.getInt("month"),
            rs.getInt("day"),
            rs.getInt("hour"),
            rs.getInt("minute"),
            rs.getBigDecimal("amount"),
            rs.getString("merchant")
    );
}
