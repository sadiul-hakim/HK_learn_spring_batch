package xyz.sadiulhakim.inter_project.pojo;

import java.math.BigDecimal;

public record BalanceUpdate(
        long id,
        BigDecimal balance
) {
}
