package doremi;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Float storeId;
    private Float price;

    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();

        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();
    }


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
