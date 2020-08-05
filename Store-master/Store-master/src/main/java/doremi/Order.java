package doremi;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private Long storeId;
    private Long cartId;
    private OrderStatus orderStatus;

    @PostPersist
    public void onPostPersist(){
        OrderCreated orderCreated = new OrderCreated();
        BeanUtils.copyProperties(this, orderCreated);
        orderCreated.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        OrderCookingStared orderCookingStared = new OrderCookingStared();
        BeanUtils.copyProperties(this, orderCookingStared);
        orderCookingStared.publishAfterCommit();


        OrderDelieveryStared orderDelieveryStared = new OrderDelieveryStared();
        BeanUtils.copyProperties(this, orderDelieveryStared);
        orderDelieveryStared.publishAfterCommit();


        OrderDeliveryCompleted orderDeliveryCompleted = new OrderDeliveryCompleted();
        BeanUtils.copyProperties(this, orderDeliveryCompleted);
        orderDeliveryCompleted.publishAfterCommit();


    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
