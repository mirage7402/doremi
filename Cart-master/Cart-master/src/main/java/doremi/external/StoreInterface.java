package doremi.external;

import doremi.Menu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="StoreInterface", url="http://localhost:8088")
public interface StoreInterface {

    @RequestMapping(method= RequestMethod.GET, path="/menus/{menuId}")
    Menu getMenuPrice(@PathVariable Long menuId);
}
