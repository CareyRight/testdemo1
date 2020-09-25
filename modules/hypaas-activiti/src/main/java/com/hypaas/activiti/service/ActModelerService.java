package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActModel;
import com.hypaas.activiti.db.ExtendActNodeSet;
import com.hypaas.activiti.db.ProcessNodeDto;
import com.hypaas.activiti.db.ProcessTaskDto;
import com.hypaas.auth.db.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;

/**
 * @ClassName ActModelerService @Description TODO @Author GuoHongKai @Date 2020/8/6 17:07 @Version
 * 1.0
 */
public interface ActModelerService {

  /*
   * @Author GuoHongKai
   * @Description 创建模型
   * @Date 17:08 2020/8/6
   * @Param [extendActModelEntity]
   * @return java.lang.String
   **/
  String CreateModeler(ExtendActModel extendActModel) throws Exception;

  /*
   * @Author GuoHongKai
   * @Description 获取流程图所有节点和连线
   * @Date 10:11 2020/8/10
   * @Param [modelId]
   * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
   **/
  List<Map<String, String>> getflows(String modelId) throws IOException;

  /*
   * @Author GuoHongKai
   * @Description 查看流程定义图
   * @Date 14:54 2020/8/11
   * @Param [modelId]
   * @return org.springframework.http.ResponseEntity<byte[]>
   **/
  ResponseEntity<byte[]> showFlowImg(String modelId);

  /*
   * @Author GuoHongKai
   * @Description 根据流程key 获取业务可用的流程
   * @Date 8:50 2020/8/18
   * @Param [actKey]
   * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
   **/
  List<Map<String, Object>> queryFlowsByActKey(String actKey);

  /*
   * @Author GuoHongKai
   * @Description 删除所有已部署流程，只为展示用
   * @Date 10:09 2020/8/18
   * @Param []
   * @return void
   **/
  void deleteAllFlows();

  /*
   * @Author GuoHongKai
   * @Description 本地flows表添加所有已部署流程，只为展示用
   * @Date 10:13 2020/8/18
   * @Param [flows]
   * @return void
   **/
  void addAllFlows(List<Map<String, Object>> flows, String actKey);

  /*
   * @Author GuoHongKai
   * @Description 获取流程第一个节点信息
   * @Date 11:37 2020/8/18
   * @Param [deployId]
   * @return void
   **/
  HashMap<String, Object> getStartFlowInfo(String deployId) throws IOException;

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:33 2020/8/18
   * @Param [nodeId]
   * @return void
   **/
  Set<User> getUsersByNodeIdModelId(String nodeId, String modelId);

  /*
   * @Author GuoHongKai
   * @Description 启动流程
   * @Date 18:26 2020/8/18
   * @Param [dto]
   * @return void
   **/
  void startFlow(ProcessTaskDto dto) throws Exception;

  /*
   * @Author GuoHongKai
   * @Description 查找节点详情
   * @Date 13:57 2020/8/19
   * @Param [nodeId, modelId]
   * @return void
   **/
  ExtendActNodeSet getNodeByNodeId(String nodeId, String modelId);

  /*
   * @Author GuoHongKai
   * @Description 查找我的待办列表
   * @Date 17:40 2020/8/19
   * @Param [params]
   * @return void
   **/
  void findMyUpcomingPage(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description 查询需要作为流程条件判断的字段
   * @Date 9:41 2020/8/21
   * @Param [taskDefinitionKey, defid]
   * @return java.util.Set<java.lang.String>
   **/
  Set<String> getNextVarNams(String taskDefinitionKey, String defid);

  /*
   * @Author GuoHongKai
   * @Description 办理任务
   * @Date 15:20 2020/8/21
   * @Param [processTaskDto, params]
   * @return void
   **/
  void doActTask(ProcessTaskDto processTaskDto, Map<String, Object> params) throws Exception;

  /*
   * @Author GuoHongKai
   * @Description 我的已办
   * @Date 15:47 2020/8/22
   * @Param [params]
   * @return void
   **/
  void myDonePage(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description 办理任务时，获取流程下一流向节点集合
   * @Date 15:13 2020/8/26
   * @Param [processTaskDto]
   * @return java.util.List<com.hypaas.activiti.entity.ProcessNodeDto>
   **/
  List<ProcessNodeDto> getNextActNodes(ProcessTaskDto processTaskDto);
}
