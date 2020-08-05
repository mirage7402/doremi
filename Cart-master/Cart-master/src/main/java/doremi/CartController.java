package doremi;

import doremi.external.StoreInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/mycart")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @RequestMapping(value = "/{storeId}/create",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Cart> selectStore(@PathVariable("storeId") Long storeId) {

        System.out.println("##### /cart/selectStore  called #####");

        if(storeId != null) {
            Cart newCart = new Cart();
            newCart.setStoreId(storeId);
            cartRepository.save(newCart);

            return new ResponseEntity<>(newCart, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{cartId}/menu/{menuId}/add",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Cart> addMenu(@PathVariable("cartId") Long cartId, @PathVariable("menuId") Long menuId) {
        System.out.println("##### /cart/addMenu  called #####");

        Cart foundCart = cartRepository.findByCartId(cartId);
        Menu foundMenu = CartApplication.applicationContext.getBean(StoreInterface.class).getMenuPrice(menuId);

        if (foundCart != null && foundMenu.getPrice()> 0) {
            foundCart.setMenuId(menuId);
            foundCart.setCartStatus(CartStatus.MENUADDED);
            foundCart.setPrice(foundMenu.getPrice());

            cartRepository.save(foundCart);

            return new ResponseEntity<>(foundCart, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{cartId}/order",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Cart> placeOrder(@PathVariable("cartId") Long cartId)
            throws Exception {
        System.out.println("##### /cart/placeOrder  called #####");

        Cart foundCart = cartRepository.findByCartId(cartId);
        if (foundCart != null) {
            foundCart.setCartStatus(CartStatus.ORDERED);
            cartRepository.save(foundCart);

            return new ResponseEntity<>(foundCart, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
