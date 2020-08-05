package doremi;

import doremi.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{


    @Value("${doremi.storeId}")
    private Long storeId;

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    public PolicyHandler(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMenuRegistered_MenuRegisteredNotification(@Payload MenuRegistered menuRegistered){

        if(menuRegistered.isMe()){
            System.out.println("##### listener MenuRegisteredNotification : " + menuRegistered.toJson());

            Menu newMenu = new Menu();
            newMenu.setMenuId(menuRegistered.getId());
            newMenu.setStoreId(storeId);
            newMenu.setDescription(menuRegistered.getDescription());
            newMenu.setMenuType(menuRegistered.getMenuType());
            newMenu.setMenuName(menuRegistered.getMenuName());

            newMenu.setMenuStatus(MenuStatus.REGISTERED);

            menuRepository.save(newMenu);
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMenuDeleted_MenuDeletedNotification(@Payload MenuDeleted menuDeleted){

        if(menuDeleted.isMe()){
            System.out.println("##### listener MenuDeletedNotification : " + menuDeleted.toJson());
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCartPaid_CreateOrder(@Payload CartPaid cartPaid){

        if(cartPaid.isMe() && cartPaid.getCartId().equals(storeId)){
            System.out.println("##### listener CreateOrder : " + cartPaid.toJson());

            Order newOrder = new Order();
            newOrder.setCartId(cartPaid.getCartId());
            newOrder.setStoreId(storeId);
            newOrder.setOrderStatus(OrderStatus.CREATED);
            orderRepository.save(newOrder);
        }
    }

}
