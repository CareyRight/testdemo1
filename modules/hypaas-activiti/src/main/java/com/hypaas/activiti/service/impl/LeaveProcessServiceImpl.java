package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.db.Leave;
import com.hypaas.activiti.db.repo.LeaveRepository;
import com.hypaas.activiti.service.LeaveProcessService;

/**
 * @ClassName LeaveServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/14 16:50 @Version
 * 1.0
 */
public class LeaveProcessServiceImpl implements LeaveProcessService {

  @Inject private LeaveRepository leaveRepository;

  /*
   * @Author GuoHongKai
   * @Description 请假单据删除
   * @Date 16:53 2020/8/14
   * @Param [id]
   * @return void
   **/
  @Override
  @Transactional
  public void leaveDelete(Long id) {
    Leave leave = leaveRepository.find(id);
    leaveRepository.remove(leave);
  }

  /*
   * @Author GuoHongKai
   * @Description 请假单据提交
   * @Date 16:53 2020/8/14
   * @Param [id]
   * @return void
   **/
  @Override
  @Transactional
  public void leaveSubmit(Long id) {}
}
