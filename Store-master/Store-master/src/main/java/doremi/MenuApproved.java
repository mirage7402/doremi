package doremi;

public class MenuApproved extends AbstractEvent {

    private Long id;

    public MenuApproved(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
