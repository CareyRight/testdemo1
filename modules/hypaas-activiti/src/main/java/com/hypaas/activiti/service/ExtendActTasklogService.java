package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActTaskLog;
import java.util.List;

/**
 * @InterfaceName ExtendActTasklogService @Description TODO @Author GuoHongKai @Date 2020/8/19
 * 9:13 @Version 1.0
 */
public interface ExtendActTasklogService {

  void save(ExtendActTaskLog tasklogEntity);

  /*
   * @Author GuoHongKai
   * @Description 查询审批进程列表
   * @Date 9:50 2020/8/20
   * @Param [params]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActTaskLog>
   **/
  List<ExtendActTaskLog> queryList(String busId);

  /*
   * @Author GuoHongKai
   * @Description 查询最后一个任务节点id
   * @Date 9:22 2020/8/21
   * @Param [busId]
   * @return java.lang.String
   **/
  String queryLastTaskId(String busId);

  /*
   * @Author GuoHongKai
   * @Description 根据任务id 更改日志
   * @Date 11:17 2020/8/22
   * @Param [tasklogEntity]
   * @return int
   **/
  int updateByTaskId(ExtendActTaskLog tasklogEntity);

  /*
   * @Author GuoHongKai
   * @Description 根据taskId 查找
   * @Date 14:52 2020/8/22
   * @Param [id]
   * @return com.hypaas.activiti.db.ExtendActTaskLog
   **/
  ExtendActTaskLog findTaskLogByTaskId(String id);
}
