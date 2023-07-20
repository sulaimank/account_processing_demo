package magicEden.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            account.setIngested(false);

            Thread.sleep(account.getCallbackTimeMs());

            // Specify that this account has been ingested
            account.setIngested(true);

            logger.info("Account " + account.getId() + ", version: " + account.getVersion() + " has been ingested");
        } catch (InterruptedException e) {
        }
    }

    public void stop() {
        logger.info("Going to stop callback for Account " + account.getId() + "version " + account.getVersion());
        runThread.interrupt();
    }
}
