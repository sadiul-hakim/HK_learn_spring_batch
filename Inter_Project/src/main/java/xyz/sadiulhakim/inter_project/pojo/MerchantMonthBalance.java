package xyz.sadiulhakim.inter_project.pojo;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record MerchantMonthBalance(
        int month,
        String merchant,
        BigDecimal balance
) {

    // Row mapper to transform query results into Java object
    public static final RowMapper<MerchantMonthBalance> ROW_MAPPER = (rs, rowNum) -> new MerchantMonthBalance(
            rs.getInt("month"),
            rs.getString("merchant"),
            rs.getBigDecimal("balance")
    );

    // Query provider to obtain month-merchant aggregation from 'bank_transaction_yearly' table
    public static PagingQueryProvider getQueryProvider() {
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("sum(amount) as balance, merchant, month");
        queryProvider.setFromClause("bank_transaction_yearly");
        queryProvider.setGroupClause("month, merchant");

        Map<String, Order> map = new HashMap<>();
        map.put("month", Order.ASCENDING);
        map.put("merchant", Order.ASCENDING);
        queryProvider.setSortKeys(map);
        return queryProvider;
    }
}
