package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.db.ExtendActTaskLog;
import com.hypaas.activiti.db.repo.ExtendActTaskLogRepository;
import com.hypaas.activiti.mapper.ExtendActTaskLogMapper;
import com.hypaas.activiti.service.ExtendActTasklogService;
import com.hypaas.db.JpaSupport;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @ClassName ExtendActTasklogServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/19
 * 9:13 @Version 1.0
 */
public class ExtendActTasklogServiceImpl extends JpaSupport implements ExtendActTasklogService {

  @Inject private ExtendActTaskLogRepository taskLogRepository;
  @Inject private ExtendActTaskLogMapper taskLogMapper;

  @Override
  @Transactional
  public void save(ExtendActTaskLog tasklogEntity) {
    taskLogRepository.save(tasklogEntity);
  }

  /*
   * @Author GuoHongKai
   * @Description 查询审批进程列表
   * @Date 9:50 2020/8/20
   * @Param [params]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActTaskLog>
   **/
  @Override
  public List<ExtendActTaskLog> queryList(String busId) {
    List<ExtendActTaskLog> extendActTaskLogs = taskLogRepository.findTaskLogsByBusId(busId).fetch();
    return extendActTaskLogs;
  }

  /*
   * @Author GuoHongKai
   * @Description 查询最后一个任务节点id
   * @Date 9:22 2020/8/21
   * @Param [busId]
   * @return java.lang.String
   **/
  @Override
  public String queryLastTaskId(String busId) {
    EntityManager em = getEntityManager();
    Query nativeQuery =
        em.createNativeQuery(
            "select max(task_id) as taskId from activiti_extend_act_task_log where bus_id = :busId");
    nativeQuery.setParameter("busId", busId);
    String taskId = (String) nativeQuery.getSingleResult();
    return taskId;
  }

  /*
   * @Author GuoHongKai
   * @Description 根据任务id 更改日志
   * @Date 11:17 2020/8/22
   * @Param [tasklogEntity]
   * @return int
   **/
  @Override
  @Transactional
  public int updateByTaskId(ExtendActTaskLog tasklogEntity) {
    ExtendActTaskLog taskLog = taskLogRepository.save(tasklogEntity);
    if (taskLog != null) return 1;
    else return 0;
  }

  /*
   * @Author GuoHongKai
   * @Description 根据taskId查找
   * @Date 14:52 2020/8/22
   * @Param [id]
   * @return com.hypaas.activiti.db.ExtendActTaskLog
   **/
  @Override
  public ExtendActTaskLog findTaskLogByTaskId(String id) {
    return taskLogRepository.findTaskLogByTaskId(id);
  }
}
