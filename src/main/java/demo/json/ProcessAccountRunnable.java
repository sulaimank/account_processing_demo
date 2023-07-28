package demo.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is manages the thread for the processing of
 * a single account.  To simulate processing, the thread
 * sleeps for a certain time (in milliseconds) based on
 * the account CallbackTimeMs.  This introduces a delay
 * @author skarmali
 */
public class ProcessAccountRunnable implements Runnable {
    public static Logger logger = LoggerFactory.getLogger(ProcessAccountRunnable.class);

    private final Account account;
    private Thread runThread;

    public ProcessAccountRunnable(Account account) {
        this.account = account;
        this.account.setProcessAccountRunnable(this);
    }

    public Account getAccount() {
        return this.account;
    }

    @Override
    public void run() {
        runThread = Thread.currentThread();
        processAccountType();
    }

    private void processAccountType() {
        logger.info("processAccountType() - Account " + account.getId());

        // Schedule the callback timer
        scheduleCallbackTimer();

        // Perform any ingestion rules
    }


    /**
     * Display a callback log when an account’s call_back_time_ms has expired. If the same account is
     * ingested with a newer version number, and the old callback has not fired yet, cancel the older
     * version’s active callback. If an old version of the same account is ingested, ignore that update.
     */
    public void scheduleCallbackTimer() {
        logger.info("Scheduling the callback timer for " + account.getCallbackTimeMs() + " milliseconds");

        try {
            // Ensure the ingested flag is false
            account.setIngested(false);

            // Put the thread to sleep.  Note this thread is running in a thread pool
            // Management of the thread is by the thread pool
            Thread.sleep(account.getCallbackTimeMs());

            // Specify that this account has been ingested
            account.setIngested(true);

            // Log that we have ingested the account
            logger.info("Account " + account.getId() + ", version: " + account.getVersion() + " has been ingested");
        } catch (InterruptedException ignored) {
            logger.info("Thread " + account.getId() + ", version " + account.getVersion() + " has been interrupted");

            account.setIngested(false);     // ensure it is not ingested
        }
    }

    public void stop() {
        logger.info("Going to stop callback for Account " + account.getId() + ", version " + account.getVersion());
        runThread.interrupt();
    }
}
