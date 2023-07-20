package magicEden.json;

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
