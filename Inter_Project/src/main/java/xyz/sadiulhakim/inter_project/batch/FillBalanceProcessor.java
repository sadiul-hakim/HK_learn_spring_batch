package xyz.sadiulhakim.inter_project.batch;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import xyz.sadiulhakim.inter_project.pojo.BalanceUpdate;
import xyz.sadiulhakim.inter_project.pojo.BankTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FillBalanceProcessor implements ItemProcessor<BankTransaction, BalanceUpdate> {

    private StepExecution stepExecution;

    public static final String BALANCE_SO_FAR = "balanceSoFar";

    @Override
    public BalanceUpdate process(BankTransaction item) throws Exception {
        if (stepExecution == null) {
            throw new RuntimeException("Can not process item without accessing the step execution");
        }

        BigDecimal newBalance = BigDecimal.valueOf(getLatestTransactionBalance())
                .setScale(2, RoundingMode.HALF_UP)
                .add(item.amount());
        BalanceUpdate balanceUpdate = new BalanceUpdate(item.id(), newBalance);
        stepExecution.getExecutionContext().putDouble(BALANCE_SO_FAR, newBalance.doubleValue());
        return balanceUpdate;
    }

    public double getLatestTransactionBalance() {
        if (stepExecution == null) {
            throw new RuntimeException("Can not get the latest balance without accessing the step execution");
        }
        // If no balance is present, start from 0
        return stepExecution.getExecutionContext().getDouble(BALANCE_SO_FAR, 0d);
    }

    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}
