package doremi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/stores")
public class MenuController {

    private final MenuRepository menuRepository;

    public MenuController(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @RequestMapping(value = "/{storeId}/menus/{menuId}/approve",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<Menu> approveMenu(@PathVariable(value = "storeId") Long storeId, @PathVariable(value = "menuId") Long menuId) {

        System.out.println("##### /menu/approveMenu  called #####");

        Menu foundMenu = menuRepository.findMenuByStoreIdAndMenuId(storeId, menuId);
        if (foundMenu != null) {
            foundMenu.setMenuStatus(MenuStatus.APPROVED);
            menuRepository.save(foundMenu);

            return new ResponseEntity<>(foundMenu, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{storeId}/menus",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<Menu>> getMenuList(@PathVariable(value = "storeId") Long storeId) {
        System.out.println("##### /menu/getMenuList  called #####");

        List<Menu> storeMenuList = menuRepository.findMenuByStoreId(storeId);
        return new ResponseEntity<>(storeMenuList, HttpStatus.OK);
    }

}
