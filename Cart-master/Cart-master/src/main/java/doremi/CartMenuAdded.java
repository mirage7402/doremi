package doremi;

public class CartMenuAdded extends AbstractEvent {

    private Long id;

    public CartMenuAdded(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
