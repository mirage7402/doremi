package doremi;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MenuRepository extends PagingAndSortingRepository<Menu, Long> {
    List<Menu> findMenuByStoreId(Long storeId);
    Menu findMenuByStoreIdAndMenuId(Long storeId, Long menuId);
}