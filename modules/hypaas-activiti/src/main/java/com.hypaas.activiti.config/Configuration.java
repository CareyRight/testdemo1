package com.hypaas.activiti.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypaas.activiti.utils.ActUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName Configuration @Description TODO @Author GuoHongKai @Date 2020/6/28 15:44 @Version 1.0
 */
@ComponentScan(value = {"com.hypaas.activiti.*"})
public class Configuration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ActUtils ActUtils() {
    return new ActUtils();
  }
}
