package doremi;

public class CartCreated extends AbstractEvent {

    private Long id;

    public CartCreated(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
