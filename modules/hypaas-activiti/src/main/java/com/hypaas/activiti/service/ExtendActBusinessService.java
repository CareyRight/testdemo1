package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActModel;
import com.hypaas.activiti.entity.ExtendActBusinessEntity;

/**
 * @InterfaceName ExtendActBusinessService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 10:20 @Version 1.0
 */
public interface ExtendActBusinessService {
  /*
   * @Author GuoHongKai
   * @Description 查询所有回调和业务相关设置
   * @Date 15:09 2020/8/11
   * @Param [modelId]
   * @return com.hypaas.activiti.db.ExtendActBusiness
   **/
  ExtendActBusinessEntity queryActBusByModelId(String modelId);

  /*
   * @Author GuoHongKai
   * @Description 根据流程key查询
   * @Date 18:38 2020/8/18
   * @Param [actKey]
   * @return com.hypaas.activiti.entity.ExtendActBusinessEntity
   **/
  ExtendActModel queryByActKey(String actKey);
}
