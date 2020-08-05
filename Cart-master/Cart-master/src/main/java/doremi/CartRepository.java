package doremi;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface CartRepository extends PagingAndSortingRepository<Cart, Long>{
    Cart findByCartId(Long cartId);
}