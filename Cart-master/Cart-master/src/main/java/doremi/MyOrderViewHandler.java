package doremi;

import doremi.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyOrderViewHandler {


    @Autowired
    private MyOrderRepository myOrderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCreated_then_CREATE_1 (@Payload OrderCreated orderCreated) {
        try {
            if (orderCreated.isMe()) {
                // view 객체 생성
                MyOrder myOrder = new MyOrder();
                // view 객체에 이벤트의 Value 를 set 함
                myOrder.setId(orderCreated.getId());
                myOrder.setCartId(orderCreated.getCartId());
                myOrder.setStatus(orderCreated.getOrderStatus());
                // view 레파지 토리에 save
                myOrderRepository.save(myOrder);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCookingStared_then_UPDATE_1(@Payload OrderCookingStared orderCookingStared) {
        try {
            if (orderCookingStared.isMe()) {
                // view 객체 조회
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderDeliveryCompleted_then_DELETE_1(@Payload OrderDeliveryCompleted orderDeliveryCompleted) {
        try {
            if (orderDeliveryCompleted.isMe()) {
                // view 레파지 토리에 삭제 쿼리
                myOrderRepository.deleteById(orderDeliveryCompleted.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}