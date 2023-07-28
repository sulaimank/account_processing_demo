package demo;

import junit.framework.TestCase;
import demo.json.Account;
import demo.json.AccountType;
import demo.processor.AccountIndexerProcessor;
import demo.utilities.JsonUtils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Test class that test various scenarios of a JSON data feed
 * containing accounts that represent Solana accounts
 * @author skarmali
 */
public class SolanaIndexerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(SolanaIndexerTest.class);

    ArrayList<Account> accounts;
    private DynamicLoadDriver jobSchedular = new DynamicLoadDriver();
    private JsonUtils jsonUtils = new JsonUtils();


    @Override
    public void setUp() {
        try {
            // Configure log4j properties
            BasicConfigurator.configure();

            accounts = jsonUtils.loadJsonAccountFile("coding-challenge-input.json");
            assertNotNull(accounts);
        } catch (Exception ex) {
            logger.error("Exception caught loading JSON data file");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        accounts.clear();
        jobSchedular.shutdown();
    }

    /**
     * Validate that we are calculating highest token value by account types
     */
    public void testHighestTokenValue() {
        accounts.forEach(account -> jobSchedular.submit(account));

        final AccountIndexerProcessor accountIndexer = jobSchedular.getAccountIndexer();
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.MASTER_EDITION), 901);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.AUCTION_DATA), 960);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.MINT), 999);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.ACCOUNT), 920);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.ESCROW), 898);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.META_DATA), 997);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.AUCTION), 836);
    }

    /**
     * If an account is missing a version, ensure that no NPE is generated
     * and version defaults to 0
     */
    public void testMissingVersion() {
        Account account1 = accounts.get(0);
        Account account2 = accounts.get(1);
        account2.setId(account1.getId());
        account2.setVersion(null);

        accounts.stream().limit(2).forEach(account -> {
            jobSchedular.submit(account);
        });

        final AccountIndexerProcessor accountIndexer = jobSchedular.getAccountIndexer();
        accountIndexer.displayHighestTokenValue();

        // Since there is only 2 accounts we are testing, each with different account types, the max tokens should equal the
        // token value in their respective account
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.MASTER_EDITION), 0);
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.MINT), account1.getTokens().intValue());
        assertEquals("Invalid highest token value", accountIndexer.getHighestTokenValueByAccountType(AccountType.META_DATA), account2.getTokens().intValue());
    }

    /**
     * 0ms - simulation starts - ID1 scheduled to be ingested 550ms (0-1000ms random) later
     * 550ms - ID1 v1 is “ingested”, we print it as indexed
     * 950ms - ID1 v1 callback fires (and we log with version 1)
     */
    public void testSingleUpdateScenario() {
        accounts.stream().limit(1).forEach(account -> {
            // Validate that the account has not been ingested
            assertFalse(account.isIngested());

            // Simulation starts after a delay
            Account submittedAccount = jobSchedular.submit(account);

            assertTrue(submittedAccount.isIngested());
        });
    }

    /**
     * 0ms - simulation starts - ID1 scheduled to be ingested 550ms (0-1000ms random) later
     * 550ms - ID1 v1 is “ingested”, we print it as indexed
     * 650ms - ID1 v3 is “ingested”, print ID1 v3 indexed, cancel active ID1 v1 callback
     * 950ms - ID1 callback fires (and we log with version 1)
     * 1050ms - ID1 v3 callback fires
     */
    public void testUpdatesWithCancellationScenario() {
        Account account1 = accounts.get(0);
        account1.setVersion(1);
        account1.setCallbackTimeMs(50000);      // set high callback so we can cancel it

        Account account2 = accounts.get(1);
        account2.setId(account1.getId());
        account2.setVersion(3);
        account2.setData(account1.getData());

        accounts.stream().limit(2).forEach(account -> {
            // Simulation starts after a delay
            Account submittedAccount = jobSchedular.submit(account);

            // v1 has been canceled due to v3 being newer
            // v3 has been ingested.
            if (submittedAccount.getVersion() == 3) {
                assertTrue(submittedAccount.isIngested());
            } else if (submittedAccount.getVersion() == 1) {
                assertFalse(submittedAccount.isIngested());
            }
        });
    }
}
