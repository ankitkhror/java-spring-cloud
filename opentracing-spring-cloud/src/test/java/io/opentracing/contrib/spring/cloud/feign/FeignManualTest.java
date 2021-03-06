package io.opentracing.contrib.spring.cloud.feign;

import static io.opentracing.contrib.spring.cloud.feign.FeignTest.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import feign.Client;
import feign.Feign;
import feign.RequestLine;
import io.opentracing.contrib.spring.cloud.MockTracingConfiguration;
import io.opentracing.contrib.spring.cloud.TestController;
import io.opentracing.contrib.spring.cloud.feign.FeignManualTest.ManualFeignConfiguration;
import io.opentracing.contrib.spring.cloud.feign.FeignTest.FeignRibbonLocalConfiguration;
import io.opentracing.mock.MockTracer;

/**
 * @author Pavol Loffay
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = {MockTracingConfiguration.class, TestController.class,
        ManualFeignConfiguration.class, FeignRibbonLocalConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class FeignManualTest {

  @Configuration
  static class ManualFeignConfiguration {
    @Autowired
    public ManualFeignConfiguration(Client client) {
      feignInterface = Feign.builder().client(client)
          .target(FeignInterface.class, "http://localService");
    }
  }

  @FeignClient(value = "localService")
  interface FeignInterface {
    @RequestLine("GET /notTraced")
    // TODO this has to be added when using spring Boot 1.4.1
    @RequestMapping(method = RequestMethod.GET, value = "/notTraced")
    String hello();
  }

  private static FeignInterface feignInterface;

  @Autowired
  private MockTracer mockTracer;

  @Test
  public void testTracedRequestDefinedUrl() throws InterruptedException {
    feignInterface.hello();
    verify(mockTracer);
  }
}
