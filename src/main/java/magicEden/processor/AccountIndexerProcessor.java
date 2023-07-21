package magicEden.processor;

import magicEden.json.Account;
import magicEden.json.AccountType;
import magicEden.json.ProcessAccountRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is the main processor.  It accepts inbound accounts and processes each one.
 * Business rules are applied:
 * 1) Display a short log message when each tuple has been indexed
 * 2) Display a message when an old callback is canceled in favor of a new one
 */
public class AccountIndexerProcessor {
    public static Logger logger = LoggerFactory.getLogger(AccountIndexerProcessor.class);

    private final ExecutorService accountPool;
    private Map<String, List<Account>> accountIdToVersionMap = new HashMap<>();

    public AccountIndexerProcessor() {
        // Create a cached thread pool.  Since processing an account is a very small task
        // a cached thread pool will provide additional threads as needed.
        accountPool = Executors.newCachedThreadPool();
    }

    private void logUniqueAccountVersionTuple(Account account) {
        // Display a short message log message to console when each (accountId + version)
        // tuple has been indexed.
        logger.info("Account (id=" + account.getId() + ", " + account.getVersion() + ") has been indexed");
    }

    public void handleAccountProcessing(Account account) {
        final String accountID = account.getId();
        final Integer newerVersion = account.getVersion();
        if (!accountIdToVersionMap.containsKey(accountID)) {
            accountIdToVersionMap.put(accountID, new ArrayList<>(List.of(account)));

            logUniqueAccountVersionTuple(account);
        } else {
            // Safe check to make sure that account has not been previously ingested
            if (account.isIngested()) {
                logger.info("The account has already been ingested, ignore the update");

                // Don't process further - ignoring update!!!
                return;
            }

            // If the account is unique log the indexing of the tuple
            List<Account> accountList = accountIdToVersionMap.get(accountID);
            if (!accountList.contains(account)) {
                logUniqueAccountVersionTuple(account);
            } else {
                logger.info("Duplicate Index Handled:: Account (id=" + accountID + ", " + account.getVersion() + ") has been indexed");
            }

            // Get the highest version.  We need to iterate whole list as versions
            // can be processed out of order (ie. v3 comes before v1)
            accountList.forEach(act -> {
                final Integer version = act.getVersion();
                if (version < newerVersion) {
                    // If the same account is ingested with a newer version number and the
                    // old callback has not fired yet, cancel the older version's active callback
                    if (!act.isIngested()) {
                        logger.info("The previous version " + act.getVersion() + " has not been ingested.  Cancel old callback in favor of the new one");
                        ProcessAccountRunnable runnable = act.getProcessAccountRunnable();

                        // Cancel old callbacks from previous versions if they have not fired
                        runnable.stop();
                    }
                }
            });

            // This map keeps track of account versions based on account id
            accountIdToVersionMap.get(accountID).add(account);
        }

        // Process this account in a thread managed by the thread pool.
        // The account is wrapped in a runnable class to manage the thread
        accountPool.execute(new ProcessAccountRunnable(account));
    }

    private Map<AccountType, IntSummaryStatistics> getTokenStats(List<Account> allAccounts) {
        // Calculate the token summary stats grouped by account type
        return allAccounts.stream().collect(Collectors.groupingBy(Account::getAccountType,
                                                                  Collectors.summarizingInt(Account::getTokens)));
    }

    /**
     * Display the highest token value by account type
     */
    public void displayHighestTokenValue() {
        logger.info("Displaying highest token value grouped by account type");

        List<Account> allAccounts = new ArrayList<>();
        accountIdToVersionMap.forEach((key, value) -> allAccounts.addAll(value));

        final Map<AccountType, IntSummaryStatistics> byAccountType = getTokenStats(allAccounts);
        byAccountType.forEach((accountType, tokenStats) -> logger.info("Account Type: " + accountType + ", Highest Token Value: " + tokenStats.getMax()));
    }

    /**
     * Get the highest token value by account type.
     * @param type - account type
     * @return int representing the highest token value for the account type
     */
    public int getHighestTokenValueByAccountType(AccountType type) {
        // Filter out all the account types that we are interested in
        List<Account> allAccounts = new ArrayList<>();
        accountIdToVersionMap.forEach((key, accounts) -> {
            List<Account> accountsByType = accounts.stream().filter(account -> account.getAccountType() == type).collect(Collectors.toList());
            allAccounts.addAll(accountsByType);
        });

        final Map<AccountType, IntSummaryStatistics> byAccountType = getTokenStats(allAccounts);
        if (!byAccountType.isEmpty()) {
            return byAccountType.get(type).getMax();
        } else {
            // Returning 0 for now since there was no match.
            logger.info("There was no " + type + " account types.  Indeterminate high token value");
            return 0;
        }
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
     */
    public void shutdown() {
        logger.info("shutdown() of thread pool");

        try {
            // Shutdown the thread pool.  The thread pool will wait for running threads
            // to finish.  No new threads added to the thread pool after this
            accountPool.shutdown();

            if (!accountPool.awaitTermination(20, TimeUnit.SECONDS)) {
                logger.error("************** Threads did not get time to finish within the 20 second window ***********");
            } else {
                // We are done processing - print the highest token value
                displayHighestTokenValue();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}