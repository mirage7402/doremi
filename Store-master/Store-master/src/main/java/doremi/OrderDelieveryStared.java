package doremi;

public class OrderDelieveryStared extends AbstractEvent {

    private Long id;

    public OrderDelieveryStared(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
