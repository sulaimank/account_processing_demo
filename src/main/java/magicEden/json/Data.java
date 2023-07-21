package magicEden.json;

/**
 * This class is a POJO that represents the Solana accounts.
 * Represents the data of the account.  All accounts that share the
 * same AccountType have the same data schema.
 * Note: This schema is fixed
 * Note: It would be better to parse JSON data elements in a polymorphic
 * deserialization but to do that would require custom deserialization
 */
public class Data {
    private String img;
    private Integer expiry;
    private Integer currentBid;
    private String mintId;

    public boolean isMintSchema() {
        return (mintId != null);
    }

    public boolean isImageSchema() {
        return (img != null);
    }

    public boolean isBidSchema() {
        return (expiry != null) && (currentBid != null);
    }

    public boolean isEmptySchema() {
        return (img == null) && (expiry == null) && (currentBid == null) && (mintId == null);
    }

    @Override
    public String toString() {
        return "Data{" +
                "img='" + img + '\'' +
                ", expiry=" + expiry +
                ", currentBid=" + currentBid +
                ", mintId='" + mintId + '\'' +
                '}';
    }
}
