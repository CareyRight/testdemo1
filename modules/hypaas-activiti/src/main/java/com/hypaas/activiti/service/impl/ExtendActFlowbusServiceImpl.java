package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.db.ExtendActFlowBus;
import com.hypaas.activiti.db.repo.ExtendActFlowBusRepository;
import com.hypaas.activiti.mapper.ExtendActFlowBusMapper;
import com.hypaas.activiti.service.ExtendActFlowbusService;

/**
 * @ClassName ExtendActFlowbusServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/19
 * 9:04 @Version 1.0
 */
public class ExtendActFlowbusServiceImpl implements ExtendActFlowbusService {

  @Inject private ExtendActFlowBusRepository extendActFlowBusRepository;
  @Inject private ExtendActFlowBusMapper actFlowBusMapper;

  @Override
  @Transactional
  public void save(ExtendActFlowBus flowBus) {
    extendActFlowBusRepository.save(flowBus);
  }

  /*
   * @Author GuoHongKai
   * @Description 根据busId修改流程表信息
   * @Date 10:12 2020/8/27
   * @Param [flowBus]
   * @return void
   **/
  @Override
  @Transactional
  public void updateByBusId(ExtendActFlowBus flowBus) {
    actFlowBusMapper.updateByBusId(flowBus);
  }

  /*
   * @Author GuoHongKai
   * @Description 查询流程基本信息
   * @Date 9:56 2020/8/21
   * @Param [instanceId, busId]
   * @return com.hypaas.activiti.db.ExtendActFlowBus
   **/
  @Override
  public ExtendActFlowBus queryByBusIdInsId(String instanceId, String busId) {
    return extendActFlowBusRepository.queryByBusIdInsId(busId, instanceId);
  }
}
