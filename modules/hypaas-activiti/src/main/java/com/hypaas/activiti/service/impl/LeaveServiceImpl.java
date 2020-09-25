package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.hypaas.activiti.db.Leave;
import com.hypaas.activiti.db.repo.LeaveRepository;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.service.LeaveService;
import com.hypaas.activiti.utils.StringUtils;

/**
 * @ClassName LeaveService @Description TODO @Author GuoHongKai @Date 2020/8/19 18:42 @Version 1.0
 */
public class LeaveServiceImpl implements LeaveService {

  @Inject private LeaveRepository leaveRepository;

  /*
   * @Author GuoHongKai
   * @Description 查询详细业务信息
   * @Date 18:42 2020/8/19
   * @Param [busId]
   * @return void
   **/
  @Override
  public Leave queryObject(String busId) {
    if (StringUtils.isEmpty(busId)) {
      new MyException("id不为空!");
    }
    return leaveRepository.findByBusId(busId);
  }
}
