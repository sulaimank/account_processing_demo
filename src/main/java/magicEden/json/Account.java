package magicEden.json;

/**
 * This class is a POJO that represents the Solana accounts.
 */
public class Account {
    // Unique identifier of the account
    private String id;

    // Type of the account
    private AccountType accountType;

    // Amount of tokens in the account
    private Integer tokens;

    // Time at which we’d like to print the contents of the account to console after it’s
    //been ingested.
    private Integer callbackTimeMs;

    // Data of the account. All accounts that share the same AccountType have the same data
    // schema. This is the information in which clients are most interested in. You can assume these
    // schemas are fixed.
    private Data data;

    // Version of the account on chain
    private Integer version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    public Integer getCallbackTimeMs() {
        return callbackTimeMs;
    }

    public void setCallbackTimeMs(Integer callbackTimeMs) {
        this.callbackTimeMs = callbackTimeMs;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Integer getVersion() {
        return (version != null) ? version : 0;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


    // Used to keep track on whether this account has been injested
    private transient boolean isIngested;

    // We save a reference to the runnable class so we have control
    // so we can stop processing this account version if a newer
    // version of the account with same id is processed
    private transient ProcessAccountRunnable processAccountRunnable;


    public Account() {
        isIngested = false;
    }

    public boolean isIngested() {
        return isIngested;
    }

    public void setIngested(boolean ingested) {
        isIngested = ingested;
    }

    public ProcessAccountRunnable getProcessAccountRunnable() {
        return processAccountRunnable;
    }

    public void setProcessAccountRunnable(ProcessAccountRunnable processAccountRunnable) {
        this.processAccountRunnable = processAccountRunnable;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", accountType=" + accountType +
                ", tokens=" + tokens +
                ", callbackTimeMs=" + callbackTimeMs +
                ", data=" + data +
                ", version=" + version +
                '}';
    }
}
