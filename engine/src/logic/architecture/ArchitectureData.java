package logic.architecture;

public enum ArchitectureData {
    I (5),
    II(100),
    III(500),
    IV(1000),
    ;
    private final int creditsCost;

    ArchitectureData(int cost) {
        this.creditsCost = cost;
    }
    public int getCreditsCost() {
        return creditsCost;
    }
}
