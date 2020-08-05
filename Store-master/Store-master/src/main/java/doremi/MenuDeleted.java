package doremi;

public class MenuDeleted extends AbstractEvent {

    private Long id;

    public MenuDeleted(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
