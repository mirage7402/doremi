package doremi.external;

public class Payment {

    private Long id;
    private Float storeId;
    private Float price;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Float getStoreId() {
        return storeId;
    }
    public void setStoreId(Float storeId) {
        this.storeId = storeId;
    }
    public Float getPrice() {
        return price;
    }
    public void setPrice(Float price) {
        this.price = price;
    }

}
