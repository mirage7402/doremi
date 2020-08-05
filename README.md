# 실습기록
KAFKA
- 토픽 생성
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic ipTVShopProject --create --partitions 1 --replication-factor 1
- 토픽 조회
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list
- 이벤트 수신
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic ipTVShopProject --from-beginning
kubectl -n kafka exec -ti my-kafka-1 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic ipTVShopProject



# (도레미 피자) Pizza매장 메뉴관리, 주문배송관리 서비스
5조의 주제는 Pizza 메뉴관리 주문 배송 서비스 입니다. 

# Table of contents

- [Pizza주문배송](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [시연](#시연)

# 서비스 시나리오

기능적 요구사항
1. 상품 Manager가 매장별 판매할 상품을 선정한다. 
2. 해당 매장에 Menu 변동에 대한 confirm을 요청한다.
3. Confirm된 Menu는 해당 매장에서 선태 가능한 Menu가 된다.
4. 고객이 주문할 매장을 선택한다.
5. 매장 선택 시점에 매장주문카트가 생성된다.
6. 메뉴를 골라서 카트에 담는다.
7. 주문완료처리하면 결제 시스템연동되어 결제수행된다.
8. 결제완료되면 해당 매장에서 주문을 받는다.
9. 매장에서 Pizza를 제작한다.
10. Pizza가 배송되고 해당 건은 완료 처리된다.

비기능적 요구사항
1. 트랜잭션:결제가 되어야만 매장에서 주문을 받는다. Sync 호출
2. 장애격리: Menu처리 승인에 상관없이 Menu수정 요청을 할 수 있다. Async (event-driven), Eventual Consistency
3. Menu에 담을때 메뉴정보 주는 관리자 서비스가 바쁘면 Cart담기 보류 (Circuit breaker Test용 요구)
4. 성능:어드민 매뉴 관리는 별도 DB (CQRS)

# 체크포인트

- 분석 설계
  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직
  ![image](https://user-images.githubusercontent.com/66579932/89254335-e0ca2900-d659-11ea-8480-ab0481808ab4.png)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://msaez.io/#/storming/AlwY0xU9WQM4n38KejdTXumIPvb2/mine/27ad58a8105899ecb335363f4ad545cc/-MDw2dZyFY4mYknntUew


![image](https://user-images.githubusercontent.com/66579932/89254361-f0497200-d659-11ea-8b5f-3a97426e8765.png)
#
![image](https://user-images.githubusercontent.com/66579932/89254385-fc353400-d659-11ea-9cc7-a9b64cf55c2f.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
#
![image](https://user-images.githubusercontent.com/66579932/89254420-09522300-d65a-11ea-9640-d1cdb3a1f5e0.png)
#
![image](https://user-images.githubusercontent.com/66579932/89254448-1838d580-d65a-11ea-971a-c5de6d0df40f.png)
#
![image](https://user-images.githubusercontent.com/66579932/89254476-2555c480-d65a-11ea-8e93-c2e8f799638f.png)
#
![image](https://user-images.githubusercontent.com/66579932/89254494-30a8f000-d65a-11ea-9503-f661da2926f4.png)
#
![image](https://user-images.githubusercontent.com/66579932/89254527-3f8fa280-d65a-11ea-9700-7d9da083af5d.png)
## 1차완성본
![image](https://user-images.githubusercontent.com/66579932/89360711-4246d200-d704-11ea-96e9-fe838deea08c.png)
##
![image](https://user-images.githubusercontent.com/66579932/89360309-f8112100-d702-11ea-943f-0b2e21367cdc.png)

## 비기능요구사항 검증
![image](https://user-images.githubusercontent.com/66579932/89360711-4246d200-d704-11ea-96e9-fe838deea08c.png)


#
![image](https://user-images.githubusercontent.com/66579932/89360320-07906a00-d703-11ea-9c49-e90cc227abd4.png)



## 
    
![image](https://user-images.githubusercontent.com/66579932/89360334-16771c80-d703-11ea-9468-a305a4314bec.png)

# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd Admin
mvn spring-boot:run

cd gateway
mvn spring-boot:run 

cd Cart
mvn spring-boot:run  

cd Payment
mvn spring-boot:run  

cd Store
mvn spring-boot:run  
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: Order, AdminMenu, StoreMenu, Cart

```
package doremi;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private Long storeId;
    private Long cartId;
    private OrderStatus orderStatus;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getStoreId() {
        return storeId;
    }
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public Long getCartId() {
        return cartId;
    }
    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어뎁터 자동생성 다양한 데이터소스 유형(H2DB, HSQLDB) 에 대한 별도의 처리가 없도록 함.\위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package doremi;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface CartRepository extends PagingAndSortingRepository<Cart, Long>{
    Cart findByCartId(Long cartId);
}```

- 적용 후 REST API 의 테스트
```
(Admin) 메뉴 추가

http POST http://localhost:8088/menus menuName=pizza3 menuType=pizza description=pizzadesc price=150.0

http GET http://localhost:8088/menus/1

(Store Manager) 상점 관리자가 Store에서 메뉴 상태 확인 (StoreId :2002)

http GET http://localhost:8088/stores/{{storeId}}/menus

http GET http://localhost:8088/stores/2002/menus

(Store Manager) 상점 관리자가 상품 Approve

http POST http://localhost:8088/stores/{{storeId}}/menus/{{menuId}}/approve

http POST http://localhost:8088/stores/2002/menus/1/approve

(Member) 카트 생성

http POST http://localhost:8088/mycart/{{storeId}}/create

http POST http://localhost:8088/mycart/2002/create

(Member) 카트에 메뉴 담기

http POST http://localhost:8088/mycart/{{cartdId}}/menu/{menuId}/add

http POST http://localhost:8088/mycart/1/menu/1/add

(Member) 카트 최종 주문

http POST http://localhost:8088/mycart/{{cartdId}}/order

http POST http://localhost:8088/mycart/1/order

(Admin) 

http GET http://localhost:8088/payments

(StoreManager) 주문 접수 상태 확인 

http GET http://localhost:8088/orders

(StoreManager) 조리 시작 

http POST http://localhost:8088/myorder/1/cooking/start

(StoreManager) 배달시작 시작 

http POST http://localhost:8088/myorder/delivery/{{orderId}} 

http POST http://localhost:8088/myorder/delivery/1

(StoreManager) 배달 종료 

http POST http://localhost:8088/myorder/delivery/{{orderId}}/complete 

http POST http://localhost:8088/myorder/delivery/1/complete
```


```



## 폴리글랏 퍼시스턴스 처리
```
Admin 은 HSQLDB, 나머지는 서비스는 H2 적용
Payment는 HSQLDB 적용
pom.xml dependency 추가
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <version>2.4.0</version>
    <scope>runtime</scope>
</dependency>
```


## 동기식 호출 과 Fallback 처리
분석단계에서의 조건 중 하나로 주문(app)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 
테스트를 위하여 Cart에서 상품 price 가져오는 부분을 동기식 호출, 서킷브레이킹, fallback 처리 하였다 (아래 소스의 @HystrixCommand(fallbackMethod = "fallbackHello") 참고)

```
# CartController.java
package doremi;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import doremi.external.StoreInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    private ResponseEntity<Cart> fallbackHello(Long cartId, Long menuId) {
        Cart blankCart = new Cart();
        blankCart.setCartId(cartId);
        blankCart.setCartStatus(CartStatus.BLANK);

        System.out.println("Hello, Fallback. cartId : " + cartId + "  menuId : " + menuId);
        return new ResponseEntity<>(blankCart, HttpStatus.OK);
    }

    @RequestMapping(value = "/{cartId}/menu/{menuId}/add",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @HystrixCommand(fallbackMethod = "fallbackHello")
    public ResponseEntity<Cart> addMenu(@PathVariable("cartId") Long cartId, @PathVariable("menuId") Long menuId) {
        System.out.println("##### /cart/addMenu  called #####");

        Cart foundCart = cartRepository.findByCartId(cartId);

        Menu foundMenu = CartApplication.applicationContext.getBean(StoreInterface.class).getMenuPrice(menuId);
        try {
            long start = System.currentTimeMillis();
            Thread.currentThread().sleep((long) (2500 + Math.random() * 600));
            System.out.println("Sleep time in ms = "+(System.currentTimeMillis()-start));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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


```



## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


관리자가 메뉴를 갱신하면, 매장 메뉴가 변경되며 이를 비 동기식으로 처리하여 고객 주문이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 메뉴갱신에 대한 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package doremi;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Menu_table")
public class Menu {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String menuName;
    private String menuType;
    private String description;
    private Float price;

    @PostPersist
    public void onPostPersist(){
        MenuRegistered menuRegistered = new MenuRegistered();
        BeanUtils.copyProperties(this, menuRegistered);
        menuRegistered.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        MenuDeleted menuDeleted = new MenuDeleted();
        BeanUtils.copyProperties(this, menuDeleted);
        menuDeleted.publishAfterCommit();
    }
```
- 메뉴 갱신 이벤트에 대한 자신의 정책 처리하도록 PolicyHandler 를 구현한다:

```
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

```


# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 GCP를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 cloudbuild.yml 에 포함되었다.
![image](https://user-images.githubusercontent.com/66579932/89374319-00c71e80-d726-11ea-8207-fb04a5969c68.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

고객이 Cart에서 주문시에 menu에서 price를 가져온다. 이때 price 조회 요청이 과도할 경우 장애격리 하도록 구현함

```
# CartApplication.java
@SpringBootApplication
@EnableBinding(KafkaProcessor.class)
@EnableFeignClients
@EnableCircuitBreaker
public class CartApplication {
    protected static ApplicationContext applicationContext;
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(CartApplication.class, args);
    }
}

# CartController.java 
@HystrixCommand(fallbackMethod = "fallbackHello")
    public ResponseEntity<Cart> addMenu(@PathVariable("cartId") Long cartId, @PathVariable("menuId") Long menuId) {
        System.out.println("##### /cart/addMenu  called #####");

# pom.xml
<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>

```


* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시
![image](https://user-images.githubusercontent.com/66579932/89378068-04f73a00-d72e-11ea-97df-a83c490f80ea.png)
```
$ siege -c3 -t30S -v --content-type "application/json" http POST http://a5e10f041e91a45ac9d806776373cc4c-979366128.us-east-2.elb.amazonaws.com:8080/mycart/2/menu/8/add

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...


```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 63.55% 가 성공하였고, 46%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy t5-admin -w
```
![image](https://user-images.githubusercontent.com/66579932/89379690-36253980-d731-11ea-86a5-10c01483b8bf.png)


## 무정지 재배포

* 
![image](https://user-images.githubusercontent.com/66579932/89377259-61f1f080-d72c-11ea-8f92-cde529f6618d.png)

```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
