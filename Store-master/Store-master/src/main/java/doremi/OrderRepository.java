package doremi;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
    Order findByOrderId(Long orderId);
    Order findByCartId(Long cartId);
}