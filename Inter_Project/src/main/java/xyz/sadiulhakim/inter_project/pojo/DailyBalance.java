package xyz.sadiulhakim.inter_project.pojo;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record DailyBalance(
        int day,
        int month,
        BigDecimal balance
) {

    // Row mapper to transform query results into Java object
    public static final RowMapper<DailyBalance> ROW_MAPPER = (rs, rowNum) -> new DailyBalance(
            rs.getInt("day"),
            rs.getInt("month"),
            rs.getBigDecimal("balance")
    );

    // Query provider to obtain daily balance aggregation from 'bank_transaction_yearly' table
    public static PagingQueryProvider getQueryProvider() {
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("sum(amount) as balance, day, month");
        queryProvider.setFromClause("bank_transaction_yearly");
        queryProvider.setGroupClause("day, month");

        Map<String, Order> map = new HashMap<>();
        map.put("month", Order.ASCENDING);
        map.put("day", Order.ASCENDING);
        queryProvider.setSortKeys(map);
        return queryProvider;
    }
}
