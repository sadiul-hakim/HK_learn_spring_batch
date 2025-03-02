package xyz.sadiulhakim.inter_project.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.inter_project.pojo.BankTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GenerateSourceDatabase {

    private final JdbcTemplate jdbcTemplate;

    // Number of records to generate
    private static final int TARGET_RECORD_NUM = 300;
    // Number of unique merchants to be used in generated records
    private static final int TARGET_UNIQUE_MERCHANT_NUM = 40;
    // Key is month number, 1-indexed, i.e. 1 is January, 12 is December; value is the number of days
    private static final Map<Integer, Integer> DAYS_IN_MONTH_MAP = new ConcurrentHashMap<>();

    static {
        DAYS_IN_MONTH_MAP.put(1, 31);
        DAYS_IN_MONTH_MAP.put(2, 28);
        DAYS_IN_MONTH_MAP.put(3, 31);
        DAYS_IN_MONTH_MAP.put(4, 30);
        DAYS_IN_MONTH_MAP.put(5, 31);
        DAYS_IN_MONTH_MAP.put(6, 30);
        DAYS_IN_MONTH_MAP.put(7, 31);
        DAYS_IN_MONTH_MAP.put(8, 31);
        DAYS_IN_MONTH_MAP.put(9, 30);
        DAYS_IN_MONTH_MAP.put(10, 31);
        DAYS_IN_MONTH_MAP.put(11, 30);
        DAYS_IN_MONTH_MAP.put(12, 31);
    }

    public GenerateSourceDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Main method re-creating table and generating records in the database
    public void generate() {

        // Initialize the schema
        SourceManagementUtils.initializeEmptyDatabase(jdbcTemplate);

        List<BankTransaction> recordsToInsert = new ArrayList<>(TARGET_RECORD_NUM);
        Random random = new Random();
        String[] merchants = generateMerchants();
        for (int i = 0; i < TARGET_RECORD_NUM; i++) {
            recordsToInsert.add(generateRecord(random, merchants));
        }

        // Sort random records chronologically
        recordsToInsert.sort((t1, t2) -> {
            if (t1.getMonth() < t2.getMonth()) {
                return -1;
            } else if (t1.getMonth() > t2.getMonth()) {
                return 1;
            } else if (t1.getDay() < t2.getDay()) {
                return -1;
            } else if (t1.getDay() > t2.getDay()) {
                return 1;
            } else if (t1.getHour() < t2.getHour()) {
                return -1;
            } else if (t1.getHour() > t2.getHour()) {
                return 1;
            } else if (t1.getMinute() < t2.getMinute()) {
                return -1;
            } else if (t1.getMinute() > t2.getMinute()) {
                return 1;
            } else {
                return t1.getAmount().compareTo(t2.getAmount());
            }
        });

        for (BankTransaction transaction : recordsToInsert) {
            // Insert records in the db, relying on auto-increment (serial) for id
            SourceManagementUtils.insertBankTransaction(transaction, jdbcTemplate);
        }

        // Print to console the success message
        System.out.println("Input source table with " + TARGET_RECORD_NUM + " records is successfully initialized");
    }

    // Generate random transaction record using pre-calculated list of merchants to use
    public static BankTransaction generateRecord(Random random, String[] merchants) {
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(DAYS_IN_MONTH_MAP.get(month)) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        double doubleAmount = ((double) random.nextInt(100000)) / 100;
        if (random.nextBoolean()) {
            doubleAmount *= -1;
        }
        BigDecimal amount = new BigDecimal(doubleAmount).setScale(2, RoundingMode.HALF_UP);
        String merchant = merchants[random.nextInt(merchants.length)];

        return new BankTransaction(-1, month, day, hour, minute, amount, merchant);
    }

    // Return array of merchant names to be used
    private static String[] generateMerchants() {
        String[] merchantsArray = new String[TARGET_UNIQUE_MERCHANT_NUM];
        for (int i = 0; i < TARGET_UNIQUE_MERCHANT_NUM; i++) {
            merchantsArray[i] = UUID.randomUUID().toString();
        }
        return merchantsArray;
    }
}
