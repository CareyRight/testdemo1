package com.hypaas.activiti.service;

import com.hypaas.activiti.db.Leave;

/**
 * @InterfaceName LeaveService @Description TODO @Author GuoHongKai @Date 2020/8/19 18:40 @Version
 * 1.0
 */
public interface LeaveService {

  /*
   * @Author GuoHongKai
   * @Description 查询详细业务信息
   * @Date 18:41 2020/8/19
   * @Param [busId]
   * @return void
   **/
  Leave queryObject(String busId);
}
