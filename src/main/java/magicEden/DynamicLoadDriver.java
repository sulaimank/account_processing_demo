package magicEden;

import magicEden.json.Account;
import magicEden.processor.AccountIndexerProcessor;
import magicEden.utilities.JsonUtils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the main entry-point for simulating inbound accounts that arrive
 * in random time (0-1 seconds apart).  To simulate data, a data file exists
 * that contains data emulating indexing of data on the blockchain.
 */
public class DynamicLoadDriver {
    public static Logger logger = LoggerFactory.getLogger(DynamicLoadDriver.class);

    private JobSchedular jobSchedular;
    private AccountIndexerProcessor accountIndexer = new AccountIndexerProcessor();

    /**
     * Lambda expression to execute
     */
    interface JobSchedular {
        Account submit(Account account);
    }

    public DynamicLoadDriver() {
        // Define lambda logic to randomly sleep to simulate a even distribution of load
        jobSchedular = (account -> {
            logger.info("*** Handling Account " + account.toString() + " ***");

            // To keep track of the number of accounts handled
            AtomicInteger accountCount = new AtomicInteger();

            final int ingestionDelay = getRandomUniformDistribution();
            logger.debug("\tGoing to delay " + ingestionDelay + " milliseconds");

            try {
                TimeUnit.MILLISECONDS.sleep(ingestionDelay);
            } catch (InterruptedException ignored) {
            }

            logger.info("Processing account.  Going to handle account " + account.getId());
            accountIndexer.handleAccountProcessing(account);
            accountCount.getAndIncrement();
            logger.debug("Processed account count " + accountCount);

            return account;
        });
    }

    /**
     * Each account comes into the system at a continuous uniform (random) distribution between 0
     * and 1000ms.
     *
     * @return double indicating milliseconds
     */
    public static int getRandomUniformDistribution() {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it upper bound inclusive
        final int min = 0;
        final int max = 1000;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Submit the account to thread pool
     *
     * @param account - Account to ingest
     * @return - processed account
     */
    public Account submit(Account account) {
        return jobSchedular.submit(account);
    }

    /**
     * Shutdown the thread pool
     */
    public void shutdown() {
        accountIndexer.shutdown();
    }

    public AccountIndexerProcessor getAccountIndexer() {
        return accountIndexer;
    }

    private void processAccounts(List<Account> accounts) {
        accounts.forEach(account -> {
            // Simulation starts after a delay
            jobSchedular.submit(account);
        });

    }

    public static void main(String[] args) {
        // Enable log4j - configure log4j properties
        BasicConfigurator.configure();

        // Create a driver that simulates account updates in an
        // asynchronous manner from JSON file
        DynamicLoadDriver driver = new DynamicLoadDriver();
        JsonUtils jsonUtils = new JsonUtils();

        ArrayList<Account> accounts = jsonUtils.loadJsonAccountFile("coding-challenge-input.json");
        driver.processAccounts(accounts);

        // safely shutdown if no accounts to process
        driver.shutdown();
    }
}
