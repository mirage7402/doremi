package doremi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/myorder")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RequestMapping(value = "/{cartId}/cooking/start",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Order> startCooking(@PathVariable("cartId") Long cartId) {
        System.out.println("##### /order/startCooking  called #####");

        Order order = orderRepository.findByCartId(cartId);
        if (order != null) {
            order.setOrderStatus(OrderStatus.COOKING);
            orderRepository.save(order);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/delivery/{orderId}",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Order> startDelivery(@PathVariable("orderId") Long orderId) {

        System.out.println("##### /order/startDelivery  called #####");

        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setOrderStatus(OrderStatus.DELIVERING);
            orderRepository.save(order);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/delivery/{orderId}/complete",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Order> completeDelivery(@PathVariable("orderId") Long orderId) {

        System.out.println("##### /order/completeDelivery  called #####");

        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
