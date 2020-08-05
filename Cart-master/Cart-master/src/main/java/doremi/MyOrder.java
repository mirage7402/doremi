package doremi;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyOrder_table")
public class MyOrder {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long cartId;
        private Long storeId;
        private String status;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getCartId() {
            return cartId;
        }

        public void setCartId(Long cartId) {
            this.cartId = cartId;
        }
        public Long getStoreId() {
            return storeId;
        }

        public void setStoreId(Long storeId) {
            this.storeId = storeId;
        }
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}
