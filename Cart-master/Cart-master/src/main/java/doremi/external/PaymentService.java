
package doremi.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "Payment", url = "http://localhost:8089")
public interface PaymentService {
    @RequestMapping(method = RequestMethod.POST, path = "/payments")
    // todo HttpResponse Code 상태로 처리
    void pay(@RequestBody Payment payment);
}