package doremi;

import javax.persistence.*;

import doremi.external.PaymentService;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Cart_table")
public class Cart {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long cartId;
    private Long storeId;
    private Long menuId;
    private Float price;
    private CartStatus cartStatus = CartStatus.CREATED;

    private String memberId = "doremi-vip1";

    @PostPersist
    public void onPostPersist(){

    }

    @PostUpdate
    public void onPostUpdate(){

        if(this.getCartStatus().equals(CartStatus.MENUADDED)) {

        } else if (this.getCartStatus().equals(CartStatus.ORDERED)) {
            CartOrdered cartOrdered = new CartOrdered();
            BeanUtils.copyProperties(this, cartOrdered);
            cartOrdered.publishAfterCommit();

            doremi.external.Payment payment = new doremi.external.Payment();
            CartApplication.applicationContext.getBean(PaymentService.class).pay(payment);

            // todo Payment 결과에 따라 처리
            CartPaid cartPaid = new CartPaid();
            BeanUtils.copyProperties(this, cartPaid);
            cartPaid.setCartStatus(CartStatus.PAID);
            cartOrdered.publishAfterCommit();
        }
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
    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public CartStatus getCartStatus() {
        return cartStatus;
    }

    public void setCartStatus(CartStatus cartStatus) {
        this.cartStatus = cartStatus;
    }
}
