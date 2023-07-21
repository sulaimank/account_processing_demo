package magicEden.processor;

import magicEden.json.Account;

import java.util.Comparator;

/**
 * This comparator is used for the priority queue that keeps
 * the accounts in order based on version.  So highest version
 * is at the top of the queue.
 */
public class AccountComparator implements Comparator<Account> {
    @Override
    public int compare(Account account1, Account account2) {
        if (account1.getVersion() < account2.getVersion()) {
            return 1;
        } else if (account1.getVersion() > account2.getVersion()) {
            return -1;
        }

        return 0;
    }
}
