package magicEden.processor;

import magicEden.json.Account;
import magicEden.json.AccountType;
import magicEden.json.ProcessAccountRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main processor.  It accepts inbound accounts and processes each one.
 * Business rules are applied:
 * 1) Display a short log message when each tuple has been indexed
 * 2) Display a message when an old callback is canceled in favor of a new one
 */
public class AccountIndexerProcessor {
    public static Logger logger = LoggerFactory.getLogger(AccountIndexerProcessor.class);

    private final ExecutorService accountPool;

    private HashSet<String> indexedTuple = new HashSet<>();
    private Map<AccountType, Integer> highestTokenValueMap;
    private Map<String, List<Account>> accountIdToVersionMap = new HashMap<>();

    public AccountIndexerProcessor() {
        // Create a cached thread pool.  Since processing an account is a very small task
        // a cached thread pool will provide additional threads as needed.
        accountPool = Executors.newCachedThreadPool();

        // Initialize token value map.  This map will contain highest token-value accounts
        // by AccountType (taking into account right version)
        highestTokenValueMap = new HashMap<>() {{
            put(AccountType.MINT, 0);
            put(AccountType.META_DATA, 0);
            put(AccountType.MASTER_EDITION, 0);
            put(AccountType.AUCTION, 0);
            put(AccountType.AUCTION_DATA, 0);
            put(AccountType.ESCROW, 0);
            put(AccountType.ACCOUNT, 0);
        }};
    }

    public void handleAccountProcessing(Account account) {
        final String accountID = account.getId();
        final Integer newerVersion = account.getVersion();
        if (accountIdToVersionMap.containsKey(accountID) == false) {
            List<Account> accountList = new ArrayList<>();
            accountList.add(account);
            accountIdToVersionMap.put(accountID, accountList);
        } else {
            // TODO - If an old version of the same account is ingested, ignore that update

            if (account.isIngested()) {
                logger.info("The account has already been ingested, ignore the update");

                // Don't process further - ignoring update!!!
                return;
            }

            // Get the highest version
            List<Account> accountList = accountIdToVersionMap.get(accountID);
            accountList.forEach(act -> {
                Integer version = act.getVersion();
                if (version < newerVersion) {
                    // If the same account is ingested with a newer version number and the
                    // old callback has not fired yet, cancel the older version's active callback
                    if (!act.isIngested()) {
                        logger.info("The previous version " + act.getVersion() + " has not been ingested.  Cancel old callback in favor of the new one");
                        ProcessAccountRunnable runnable = act.getProcessAccountRunnable();
                        runnable.stop();
                    }
                }
            });

            accountIdToVersionMap.get(accountID).add(account);

            // Cancel old callbacks from previous versions if they have not fired
        }

        // Display a short message log message to console when each (accountId + version)
        // tuple has been indexed.
        final String tuple = account.getId() + account.getVersion();
        if (!indexedTuple.contains(tuple)) {
            logger.info("Account (id=" + accountID + ", " + account.getVersion() + ") has been indexed");
            indexedTuple.add(tuple);
        } else {
            logger.info("Duplicate Index Handled:: Account (id=" + accountID + ", " + account.getVersion() + ") has been indexed");
        }

        // Calculate highest token-value
        // TODO - is it more efficient to always set the map or calculate max and then set when there is a max
        Integer highestToken = highestTokenValueMap.get(account.getAccountType());
        highestTokenValueMap.put(account.getAccountType(), Math.max(highestToken, account.getTokens()));

        accountPool.execute(new ProcessAccountRunnable(account));
    }

    public void displayHighestTokenValue() {
        highestTokenValueMap.forEach((accountType, value) -> {
            logger.info("Account Type: " + accountType + ", Highest Token Value: " + value);
        });
    }

    public int getHighestTokenValueByAccountType(AccountType type) {
        return highestTokenValueMap.get(type);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
     */
    public void shutdown() {
        logger.info("shutdown() hook - display highest token value and gracefully shutdown thread pool");

        displayHighestTokenValue();

        accountPool.shutdown();
    }
}