package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActFlowBus;

/**
 * @InterfaceName ExtendActFlowbusService @Description TODO @Author GuoHongKai @Date 2020/8/19
 * 9:04 @Version 1.0
 */
public interface ExtendActFlowbusService {

  void save(ExtendActFlowBus flowBus);

  void updateByBusId(ExtendActFlowBus flowbusEntity);

  /*
   * @Author GuoHongKai
   * @Description 查询流程基本信息
   * @Date 9:40 2020/8/21
   * @Param [instanceId, busId]
   * @return com.hypaas.activiti.db.ExtendActFlowBus
   **/
  ExtendActFlowBus queryByBusIdInsId(String instanceId, String busId);
}
