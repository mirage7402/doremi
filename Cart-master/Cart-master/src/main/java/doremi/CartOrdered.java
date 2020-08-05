package doremi;

public class CartOrdered extends AbstractEvent {

    private Long id;

    public CartOrdered(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
