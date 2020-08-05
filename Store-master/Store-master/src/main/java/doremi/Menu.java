package doremi;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Menu_table")
public class Menu {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long menuId;
    private String menuName;
    private String menuType;
    private MenuStatus menuStatus;
    private String description;
    private Long storeId;

    @PostPersist
    public void onPostPersist(){
//        MenuRegistered menuRegistered = new MenuRegistered();
//        BeanUtils.copyProperties(this, menuRegistered);
//        menuRegistered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
//        MenuApproved menuApproved = new MenuApproved();
//        BeanUtils.copyProperties(this, menuApproved);
//        menuApproved.publishAfterCommit();
    }

    @PrePersist
    public void onPrePersist(){
//        MenuDeleted menuDeleted = new MenuDeleted();
//        BeanUtils.copyProperties(this, menuDeleted);
//        menuDeleted.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public MenuStatus getMenuStatus() {
        return menuStatus;
    }

    public void setMenuStatus(MenuStatus menuStatus) {
        this.menuStatus = menuStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }




}
