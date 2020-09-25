package com.hypaas.activiti.service;

/**
 * @InterfaceName LeaveService @Description TODO @Author GuoHongKai @Date 2020/8/14 16:50 @Version
 * 1.0
 */
public interface LeaveProcessService {

  /*
   * @Author GuoHongKai
   * @Description 单据删除
   * @Date 16:53 2020/8/14
   * @Param [id]
   * @return void
   **/
  void leaveDelete(Long id);

  /*
   * @Author GuoHongKai
   * @Description 单据提交
   * @Date 16:53 2020/8/14
   * @Param [id]
   * @return void
   **/
  void leaveSubmit(Long id);
}
