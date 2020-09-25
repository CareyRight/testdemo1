package com.hypaas.activiti.controller;

import com.google.inject.Inject;
import com.hypaas.activiti.db.*;
import com.hypaas.activiti.db.repo.FlowsRepository;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.service.*;
import com.hypaas.activiti.utils.BeanMapUtils;
import com.hypaas.activiti.utils.StringTool;
import com.hypaas.activiti.utils.StringUtils;
import com.hypaas.auth.db.User;
import com.hypaas.i18n.I18n;
import com.hypaas.meta.schema.actions.ActionView;
import com.hypaas.rpc.ActionRequest;
import com.hypaas.rpc.ActionResponse;
import java.io.IOException;
import java.util.*;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * @ClassName ExtendActDealController @Description 流程办理相关操作 @Author GuoHongKai @Date 2020/8/13
 * 19:00 @Version 1.0
 */
public class ExtendActDealController {

  @Inject private ActModelerService actModelerService;
  @Inject private ExtendActTasklogService tasklogService;
  @Inject private TaskService taskService;
  @Inject private ExtendActNodesetService nodesetService;
  @Inject private ExtendActFlowbusService flowbusService;
  @Inject private ExtendActBusinessService businessService;

  /*
   * @Author GuoHongKai
   * @Description 根据流程key 获取业务可用的流程
   * @Date 9:12 2020/8/18
   * @Param [actKey]
   * @return void
   **/
  public List<Map<String, Object>> queryFlowsByActKey(String actKey) {
    List<Map<String, Object>> flows = actModelerService.queryFlowsByActKey(actKey);
    return flows;
  }

  /*
   * @Author GuoHongKai
   * @Description 获取流程第一个节点信息
   * @Date 11:36 2020/8/18
   * @Param [deployId]
   * @return void
   **/
  public HashMap<String, Object> getStartFlowInfo(String deployId) throws IOException {
    HashMap<String, Object> startFlowInfo = actModelerService.getStartFlowInfo(deployId);
    return startFlowInfo;
  }

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:29 2020/8/18
   * @Param
   * @return
   **/
  public Set<User> getUsersByNodeIdModelId(String nodeId, String modelId) {
    Set<User> users = actModelerService.getUsersByNodeIdModelId(nodeId, modelId);
    return users;
  }

  /*
   * @Author GuoHongKai
   * @Description 选定提交流程时加载相应的参数
   * @Date 11:51 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void loadParams(ActionRequest request, ActionResponse response) throws IOException {
    ProcessTaskDto dto = request.getContext().asType(ProcessTaskDto.class);
    Flows flow = dto.getDefId();
    // 获取第一节点
    HashMap<String, Object> map = getStartFlowInfo(flow.getDeploymentId());
    String nodeId = (String) map.get("nodeId");
    ExtendActNodeSet nodeSet =
        actModelerService.getNodeByNodeId((String) map.get("nodeId"), (String) map.get("modelId"));

    // 根据actKey获取业务类型名称
    ExtendActModel list = businessService.queryByActKey(flow.getActKey());

    response.setValue("actKey", flow.getActKey());
    response.setValue("nodeType", nodeSet.getNodeType().getValue());
  }

  /*
   * @Author GuoHongKai
   * @Description 设置审批人域
   * @Date 17:05 2020/8/18
   * @Param [request, response]
   * @return void
   **/
  public void setUserDomain(ActionRequest request, ActionResponse response) throws IOException {
    ProcessTaskDto dto = request.getContext().asType(ProcessTaskDto.class);
    Flows flow = dto.getDefId();
    String actKey = dto.getActKey();
    // 获取第一节点
    HashMap<String, Object> map = getStartFlowInfo(flow.getDeploymentId());
    // 获取该节点可用的审批人
    Set<User> users =
        getUsersByNodeIdModelId((String) map.get("nodeId"), (String) map.get("modelId"));
    String idList = StringTool.getIdListString(users);
    response.setAttr("nextUserIds", "domain", "self.id IN (" + idList + ")");
  }

  /*
   * @Author GuoHongKai
   * @Description 启动流程
   * @Date 18:25 2020/8/18
   * @Param [processTaskDto] 任务dto
   * @return void
   **/
  public void startFlow(ActionRequest request, ActionResponse response) {
    ProcessTaskDto dto = request.getContext().asType(ProcessTaskDto.class);
    try {
      actModelerService.startFlow(dto);
      response.setFlash("提交成功!");
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash("提交失败!");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 我的待办列表
   * @Date 17:33 2020/8/19
   * @Param [model, code, busId, request]
   * @return void
   **/
  public void myUpcoming(ActionRequest request, ActionResponse response) {
    Map<String, Object> params = new HashMap<>();
    params.put("code", "code");
    params.put("busId", "busId");
    actModelerService.findMyUpcomingPage(params);
  }

  /*
   * @Author GuoHongKai
   * @Description 我的已办列表
   * @Date 17:33 2020/8/19
   * @Param [model, code, busId, request]
   * @return void
   **/
  public void myDoneList(ActionRequest request, ActionResponse response) {
    Map<String, Object> params = new HashMap<>();
    params.put("code", null);
    params.put("busId", null);
    actModelerService.myDonePage(params);
  }

  /*
   * @Author GuoHongKai
   * @Description 办理任务Tab
   * @Date 17:35 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void flowInfoTab(ActionRequest request, ActionResponse response) {}

  /*
   * @Author GuoHongKai
   * @Description 流程信息
   * @Date 17:35 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void flowInfoHtml(ActionRequest request, ActionResponse response) {
    String busId = (String) request.getContext().get("busId");
    // 暂时没用，因为busId 在日志表里是唯一的
    String actKey = (String) request.getContext().get("actKey");
    // ExtendActFlowBus flowBus = request.getContext().asType(ExtendActFlowBus.class);
    // 查询出来，但是展示不了
    List<ExtendActTaskLog> tasklogEntities = tasklogService.queryList(busId);
    response.setView(
        ActionView.define(I18n.get("Process Task Log"))
            .model(ExtendActTaskLog.class.getName())
            .add("grid", "ExtendActTaskLog-grid")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("width", "500")
            .param("popup-save", "false")
            .param("show-confirm", "false")
            .domain("self.busId = '" + busId + "'")
            .map());
  }

  /*
   * @Author GuoHongKai
   * @Description 办理任务时查询业务可更改的字段和必要的流程相关信息
   * @Date 17:35 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void getChangeFileds(ActionRequest request, ActionResponse response) {
    ProcessTaskDto processTaskDto = request.getContext().asType(ProcessTaskDto.class);
    String defId = (String) request.getContext().get("_defId");
    if (StringUtils.isEmpty(processTaskDto.getTaskId())) {
      throw new MyException("任务id不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getInstanceId())) {
      throw new MyException("流程实例id不能为空");
    }
    if (StringUtils.isEmpty(defId)) {
      throw new MyException("流程定义id不能为空");
    }
    Task task = taskService.createTaskQuery().taskId(processTaskDto.getTaskId()).singleResult();
    // 查询可更改字段
    ExtendActNodeSet nodesetEntity = nodesetService.queryByNodeId(task.getTaskDefinitionKey());
    // 查询需要作为流程条件判断的字段
    Set<String> nextVarNams = actModelerService.getNextVarNams(task.getTaskDefinitionKey(), defId);
    String[] changFile = {};
    if (!StringUtils.isEmpty(nodesetEntity.getChangeFiles())) {
      changFile = nodesetEntity.getChangeFiles().split(",");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 办理任务时，获取下一个节点的信息
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public List<ProcessNodeDto> getNextActNodes(ProcessTaskDto processTaskDto) {
    List<ProcessNodeDto> nextActNodes = actModelerService.getNextActNodes(processTaskDto);
    return nextActNodes;
  }

  /*
   * @Author GuoHongKai
   * @Description 转到审批任务选择下一级审批者页面
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void toDoActTaskView(ActionRequest request, ActionResponse response) {
    // 查询流程基本信息
    ExtendActTaskLog taskLog = request.getContext().asType(ExtendActTaskLog.class);
    List<ExtendActTaskLog> tasklogEntities = tasklogService.queryList(taskLog.getBusId());
    // 查询当前最后一个任务节点id
    String taskId = tasklogService.queryLastTaskId(taskLog.getBusId());
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    // 查询需要作为流程条件判断的字段
    Set<String> nextVarNams =
        actModelerService.getNextVarNams(task.getTaskDefinitionKey(), taskLog.getDefId());
    // 查询可更改字段
    ExtendActNodeSet nodesetEntity = nodesetService.queryByNodeId(task.getTaskDefinitionKey());
    // 查询流程基本信息
    ExtendActFlowBus flowbus =
        flowbusService.queryByBusIdInsId(taskLog.getInstanceId(), taskLog.getBusId());
    // 办理任务时，获取下一个节点的信息
    ProcessTaskDto processTaskDto = new ProcessTaskDto();
    Flows flow = new Flows();
    flow.setDefid(flowbus.getDefid());
    processTaskDto.setDefId(flow);
    processTaskDto.setTaskId(taskId);
    if (processTaskDto.getVarName() == null && processTaskDto.getVarValue() == null) {
      processTaskDto.setVarName("");
      processTaskDto.setVarValue("");
    }
    List<ProcessNodeDto> nextActNodes = getNextActNodes(processTaskDto);

    response.setView(
        ActionView.define(I18n.get("Select User"))
            .model(ProcessTaskDto.class.getName())
            .add("form", "process-task-dto-select-user-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("width", "500")
            .param("popup-save", "false")
            .param("show-confirm", "false")
            .context("_defId", flowbus.getDefid())
            .context("_instanceId", flowbus.getInstanceId())
            .context("_taskId", taskId)
            .context("_busId", flowbus.getBusId())
            .context("_nodesetEntity", nodesetEntity)
            .context("_nextActNodes", nextActNodes)
            .map());
  }

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:29 2020/8/18
   * @Param
   * @return
   **/
  public void getUsersByNodeIdModelId(ActionRequest request, ActionResponse response) {
    LinkedHashMap<String, String> nodesetEntity =
        (LinkedHashMap) request.getContext().get("nodesetEntity");

    List<LinkedHashMap<String, String>> nextActNodes =
        (List<LinkedHashMap<String, String>>) request.getContext().get("nextActNodes");
    LinkedHashMap<String, String> processNodeDto =
        (LinkedHashMap<String, String>) nextActNodes.get(0);

    Set<User> users =
        actModelerService.getUsersByNodeIdModelId(
            processNodeDto.get("nodeId"), nodesetEntity.get("modelId"));
    String idList = StringTool.getIdListString(users);
    response.setAttr("nextUserIds", "domain", "self.id IN (" + idList + ")");
  }

  @Inject private FlowsRepository flowsRepository;

  /*
   * @Author GuoHongKai
   * @Description 办理任务
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void doActTask(ActionRequest request, ActionResponse response) {
    ProcessTaskDto processTaskDto = request.getContext().asType(ProcessTaskDto.class);
    ProcessTaskDto temp = new ProcessTaskDto();
    BeanUtils.copyProperties(processTaskDto, temp);
    String defId = (String) request.getContext().get("_defId");
    Flows flows = flowsRepository.findByDefId(defId);
    temp.setDefId(flows);
    try {
      Map<String, Object> parameterMap = BeanMapUtils.beanToMap(temp);
      Map<String, Object> params = new LinkedCaseInsensitiveMap<>();
      for (String key : parameterMap.keySet()) {
        params.put(key, parameterMap.get(key));
      }
      actModelerService.doActTask(temp, params);
      response.setFlash("办理任务成功");
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash("办理任务失败");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 驳回到任务发起人，重新编辑提交
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void backStartUser(ActionRequest request, ActionResponse response) {}

  /*
   * @Author GuoHongKai
   * @Description 转到转办页面
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void toTurnToDo(ActionRequest request, ActionResponse response) {}

  /*
   * @Author GuoHongKai
   * @Description 转办
   * @Date 17:36 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void turnToDo(ActionRequest request, ActionResponse response) {}
}
