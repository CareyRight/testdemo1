package com.hypaas.activiti.service.impl;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.common.Constant;
import com.hypaas.activiti.db.*;
import com.hypaas.activiti.db.repo.ExtendActModelRepository;
import com.hypaas.activiti.db.repo.ExtendActNodeSetRepository;
import com.hypaas.activiti.db.repo.FlowsRepository;
import com.hypaas.activiti.entity.TableInfo;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.mapper.ActExtendMapper;
import com.hypaas.activiti.service.*;
import com.hypaas.activiti.utils.*;
import com.hypaas.auth.AuthUtils;
import com.hypaas.auth.db.User;
import com.hypaas.db.JpaSupport;
import com.hypaas.db.annotations.ActField;
import com.hypaas.db.annotations.ActTable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @ClassName ActModelerServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/6
 * 17:09 @Version 1.0
 */
public class ActModelerServiceImpl extends JpaSupport implements ActModelerService {

  @Inject private ObjectMapper objectMapper;
  @Inject private RepositoryService repositoryService;
  @Inject private ExtendActModelRepository actModelRepository;
  @Inject private ExtendActNodeSetRepository nodeSetRepository;
  @Inject private FlowsRepository flowsRepository;
  @Inject private ExtendActNodesetService nodesetService;
  @Inject private ExtendActBusinessService businessService;
  @Inject private RuntimeService runtimeService;
  @Inject private ExtendActFlowbusService flowbusService;
  @Inject private TaskService taskService;
  @Inject private ExtendActTasklogService tasklogService;
  @Inject private ActExtendMapper actExtendDao;
  @Inject private ExtendActNodeFieldService fieldService;
  @Inject private ActUtils actUtils;
  @Inject private HistoryService historyService;

  /*
   * @Author GuoHongKai
   * @Description 保存模型
   * @Date 17:09 2020/8/6
   * @Param [extendActModel]
   * @return java.lang.String
   **/
  @Override
  @Transactional
  public String CreateModeler(ExtendActModel extendActModel) throws Exception {
    // editorInfo
    ObjectNode editorNode = objectMapper.createObjectNode();
    ObjectNode stencilSetNode = objectMapper.createObjectNode();
    stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
    editorNode.put("stencilset", stencilSetNode);
    Model model = repositoryService.getModel(extendActModel.getModelId());
    if (model != null) {
      repositoryService.deleteModel(model.getId());
    }
    // 构建模型
    model = repositoryService.newModel();
    // metaInfo 元信息
    ObjectNode metaNode = objectMapper.createObjectNode();
    metaNode.put(MODEL_NAME, extendActModel.getName());
    metaNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
    metaNode.put(MODEL_DESCRIPTION, extendActModel.getDescription());
    model.setName(extendActModel.getName());
    model.setMetaInfo(metaNode.toString());
    // 保存模型
    repositoryService.saveModel(model);
    repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
    // 保存act_model表数据流程定义文件
    serviceModelSave(
        model.getId(),
        extendActModel.getBpmnXml(),
        extendActModel.getName(),
        extendActModel.getDescription());
    // 保存模型扩展表
    extendActModel.setActVersion(model.getVersion());
    extendActModel.setModelId(model.getId());
    extendActModel.setStatus(
        StringUtils.isEmpty(model.getDeploymentId())
            ? ActModelStatus.UNPUBLISHED
            : ActModelStatus.PUBLISHED);
    extendActModel.setDeploymentId(model.getDeploymentId());
    ExtendActModel temp = new ExtendActModel();
    BeanUtils.copyProperties(extendActModel, temp);
    actModelRepository.save(temp);
    List<ExtendActNodeSet> fetch =
        nodeSetRepository.findNodeSetByModelId(temp.getModelId()).fetch();
    if (fetch != null && fetch.size() != 0) {
      for (int i = 0; i < fetch.size(); i++) {
        ExtendActNodeSet node = fetch.get(i);
        nodeSetRepository.remove(node);
      }
    }
    return model.getId();
  }

  // 保存文件模型
  public void serviceModelSave(String modelId, String bpmnXml, String name, String description)
      throws Exception {
    try {

      // String unescapeXml = StringUtils.unescapeHtml(xml);//因过滤处理XSS时会对<,>等字符转码，此处需将字符串还原
      InputStream in_nocode = new ByteArrayInputStream(bpmnXml.getBytes("UTF-8"));
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
      XMLStreamReader reader = xmlFactory.createXMLStreamReader(in_nocode);

      BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
      BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(reader);

      BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
      JsonNode j = jsonConverter.convertToJson(bpmnModel);

      byte[] modelEditorSource = new ObjectMapper().writeValueAsBytes(j);

      MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>();
      values.add("json_xml", new String(modelEditorSource, "UTF-8"));
      values.add("svg_xml", "");
      values.add("name", name);
      values.add("description", description);

      Model model = repositoryService.getModel(modelId);
      ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
      modelJson.put(MODEL_NAME, name);
      modelJson.put(MODEL_DESCRIPTION, description);
      model.setMetaInfo(modelJson.toString());
      model.setName(name);

      repositoryService.saveModel(model);

      repositoryService.addModelEditorSource(
          model.getId(), values.getFirst("json_xml").getBytes("utf-8"));

      InputStream svgStream =
          new ByteArrayInputStream(values.getFirst("svg_xml").getBytes("utf-8"));
      TranscoderInput input = new TranscoderInput(svgStream);

      PNGTranscoder transcoder = new PNGTranscoder();
      // Setup output
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      TranscoderOutput output = new TranscoderOutput(outStream);
      // Do the transformation
      //      transcoder.transcode(input, output);
      //      final byte[] result = outStream.toByteArray();
      //      repositoryService.addModelEditorSourceExtra(model.getId(), result);
      outStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw new ActivitiException("Error saving model", e);
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 获取流程图所有节点和连线
   * @Date 16:17 2020/8/24
   * @Param [modelId]
   * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
   **/
  @Override
  public List<Map<String, String>> getflows(String modelId) throws IOException {
    // 转换
    JsonNode jsonNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelId));
    BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
    // 取第一个流程,注：不包括子流程 待开发
    if (bpmnModel.getProcesses().size() < 1) {
      return null;
    }
    Process process = bpmnModel.getProcesses().get(0);
    Collection<FlowElement> flowElements = process.getFlowElements();
    // 取得其中关键数据
    List<Map<String, String>> lists = new ArrayList<>();
    Map<String, String> tempmap = null;
    Map<String, Map<String, String>> allmap = new HashMap<>();
    for (FlowElement flowElement : flowElements) {
      tempmap = new HashMap<>();
      tempmap.put("treeId", flowElement.getId());
      tempmap.put("modelId", modelId);
      if (flowElement instanceof StartEvent) {
        tempmap.put("treeName", "开始节点");
        tempmap.put("type", "1");
        // tempmap.put("icon", contextPath + "/statics/images/sys/none.png");
      } else if (flowElement instanceof UserTask) {
        tempmap.put("type", "2");
        tempmap.put("treeName", flowElement.getName());
        // tempmap.put("icon", contextPath + "/statics/images/sys/typeuser.png");
      } else if (flowElement instanceof ExclusiveGateway) {
        tempmap.put("type", "3");
        tempmap.put("treeName", flowElement.getName());
        // tempmap.put("icon", contextPath + "/statics/images/sys/exclusive.png");
      } else if (flowElement instanceof SequenceFlow) {
        tempmap.put("type", "4");
        tempmap.put("treeName", flowElement.getName());
        // tempmap.put("icon", contextPath + "/statics/images/sys/sequenceflow.png");
      } else if (flowElement instanceof ParallelGateway) {
        tempmap.put("type", "6");
        tempmap.put("treeName", flowElement.getName());
        // tempmap.put("icon", contextPath + "/statics/images/sys/parallelGateway.png");
      } else if (flowElement instanceof InclusiveGateway) {
        tempmap.put("type", "7");
        tempmap.put("treeName", flowElement.getName());
        // tempmap.put("icon", contextPath + "/statics/images/sys/inclusiveGateway.png");
      } else if (flowElement instanceof EndEvent) {
        tempmap.put("type", "5");
        if (StringUtils.isNotEmpty(flowElement.getName())) {
          tempmap.put("treeName", flowElement.getName());
        } else {
          tempmap.put("treeName", "结束");
        }
        // tempmap.put("icon", contextPath + "/statics/images/sys/endnone.png");
      }
      String pid = "0";
      if (flowElement instanceof SequenceFlow) {
        pid = ((SequenceFlow) flowElement).getSourceRef();
        tempmap.put("tarid", ((SequenceFlow) flowElement).getTargetRef());
        lists.add(tempmap);
      } else {
        List<SequenceFlow> sqlist = ((FlowNode) flowElement).getIncomingFlows();
        if (sqlist != null && sqlist.size() > 0) {
          SequenceFlow tem1 = sqlist.get(0);
          pid = tem1.getSourceRef();
        }
      }
      tempmap.put("treePid", pid);
      allmap.put(flowElement.getId(), tempmap);
    }
    for (Map<String, String> map : lists) {
      String pid = map.get("treePid");
      // 如果该元素的父节点不为空 ，且父节点是 分支类型的
      if (allmap.get(pid) != null && "3".equals(allmap.get(pid).get("type"))) {
        allmap.get(map.get("tarid")).put("treePid", map.get("treeId"));
      } else {
        allmap.remove(map.get("treeId"));
      }
    }
    lists.clear();
    for (Map.Entry<String, Map<String, String>> entry : allmap.entrySet()) {
      String typex = entry.getValue().get("type");
      if ("2".equals(typex)) {
        entry.getValue().put("treePid", "0");
      } else if ("1".equals(typex)) {
        continue;
      }
      lists.add(entry.getValue());
    }
    return lists;
  }

  @Override
  public ResponseEntity<byte[]> showFlowImg(String modelId) {
    if (StringUtils.isEmpty(modelId)) {
      throw new MyException("流程模型id不能为空!");
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      byte[] bytes = repositoryService.getModelEditorSourceExtra(modelId);
      return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.CREATED);
    } catch (Exception e) {
      throw new MyException("流程图片加载失败!");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 根据流程key 获取业务可用的流程
   * @Date 8:50 2020/8/18
   * @Param [actKey]
   * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
   **/
  @Override
  public List<Map<String, Object>> queryFlowsByActKey(String actKey) {
    EntityManager em = getEntityManager();
    Query query =
        em.createNativeQuery(
            "select id_ defid, name_ name, deployment_Id_ deploymentId, description_ description from act_re_procdef where deployment_Id_ IN ( SELECT deployment_Id_ FROM act_re_model WHERE key_ = :actKey)");
    query.setParameter("actKey", actKey);
    List resultList = query.getResultList();
    String[] fields = {"defid", "name", "deploymentId", "description"};
    List map = ResultMapUtils.toResultMap(resultList, fields);
    return map;
  }

  /*
   * @Author GuoHongKai
   * @Description 删除所有已部署流程，只为展示用
   * @Date 10:09 2020/8/18
   * @Param []
   * @return void
   **/
  @Override
  @Transactional
  public void deleteAllFlows() {
    EntityManager em = getEntityManager();
    Query query = em.createNativeQuery("delete from activiti_flows");
    query.executeUpdate();
  }

  /*
   * @Author GuoHongKai
   * @Description 本地flows表添加所有已部署流程，只为展示用
   * @Date 10:13 2020/8/18
   * @Param [flows]
   * @return void
   **/
  @Override
  @Transactional
  public void addAllFlows(List<Map<String, Object>> flows, String actKey) {
    for (int i = 0; i < flows.size(); i++) {
      Flows flow = new Flows();
      Map<String, Object> obj = flows.get(i);
      flow.setDefid((String) obj.get("defid"));
      flow.setName((String) obj.get("name"));
      flow.setDeploymentId((String) obj.get("deploymentId"));
      flow.setDescription((String) obj.get("description"));
      flow.setActKey(actKey);
      flowsRepository.save(flow);
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 获取流程第一个节点信息
   * @Date 11:37 2020/8/18
   * @Param [deployId]
   * @return void
   **/
  @Override
  public HashMap<String, Object> getStartFlowInfo(String deployId) throws IOException {
    Deployment deployment =
        repositoryService.createDeploymentQuery().deploymentId(deployId).singleResult();
    Model model =
        repositoryService.createModelQuery().deploymentId(deployment.getId()).singleResult();
    byte[] bytes = repositoryService.getModelEditorSource(model.getId());
    JsonNode jsonNode = objectMapper.readTree(bytes);
    BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
    Process process = bpmnModel.getProcesses().get(0);
    List<FlowElement> flowElements = (List<FlowElement>) process.getFlowElements();
    // 流程的开始节点
    StartEvent startEvent = null;
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof StartEvent) {
        startEvent = (StartEvent) flowElement;
        break;
      }
    }
    FlowElement fe = null;
    // 获取开始的出口流向
    SequenceFlow sequenceFlow = startEvent.getOutgoingFlows().get(0);
    for (FlowElement flowElement : flowElements) {
      // 抛出异常，开始节点后的第一个节点不为userTask
      if (flowElement.getId().equals(sequenceFlow.getTargetRef())) {
        if (flowElement instanceof UserTask || flowElement instanceof EndEvent) {
          fe = flowElement;
          break;
        } else {
          throw new MyException("流程设计错误，【开始】之后只能是审批节点或结束节点");
        }
      }
    }
    if (fe != null) {
      // 查询该节点的类型
      ExtendActNodeSet nodesetEntity =
          nodesetService.queryByNodeIdModelId(fe.getId(), model.getId());
      if (nodesetEntity == null || StringUtils.isEmpty(nodesetEntity.getNodeType().getValue())) {
        throw new MyException("流程设计错误，【开始】之后只能是审批节点或结束节点");
      }
      HashMap<String, Object> result = new HashMap<>();
      result.put("nodeId", fe.getId());
      result.put("modelId", model.getId());
      result.put("nodeType", nodesetEntity.getNodeType());
      result.put("nodeAction", nodesetEntity.getNodeAction());
      // return
      return result;
    }
    return null;
  }

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:33 2020/8/18
   * @Param [nodeId]
   * @return void
   **/
  @Override
  public Set<User> getUsersByNodeIdModelId(String nodeId, String modelId) {
    Set<User> users = nodesetService.getUsersByNodeIdModelId(nodeId, modelId);
    return users;
  }

  /*
   * @Author GuoHongKai
   * @Description 启动流程
   * @Date 18:27 2020/8/18
   * @Param [dto]
   * @return void
   **/
  @Override
  @Transactional
  public void startFlow(ProcessTaskDto processTaskDto) throws Exception {
    if (StringUtils.isEmpty(processTaskDto.getActKey())) {
      throw new MyException("流程actKey不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getBusId())) {
      throw new MyException("业务ID不能为空");
    }
    if (processTaskDto.getDefId() == null) {
      throw new MyException("流程定义ID不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getNodeType())) {
      throw new MyException("节点类型不能为空");
    }
    if (processTaskDto.getNextUserIds() == null && !"5".equals(processTaskDto.getNodeType())) {
      throw new MyException("处理人不能为空");
    }
    // 查询流程业务关联信息
    ExtendActModel businessEntity = businessService.queryByActKey(processTaskDto.getActKey());
    Class<?> clazz = Class.forName(businessEntity.getClassEntityStr().getFullName());
    ActTable actTable = clazz.getAnnotation(ActTable.class);
    Map<String, Object> params = new HashMap<>();
    params.put(TableInfo.TAB_TABLENAME, actTable.tableName());
    params.put(TableInfo.TAB_PKNAME, actTable.pkName());
    params.put(TableInfo.TAB_ID, processTaskDto.getBusId());
    // 查询当前要提交流程的业务记录
    Map<String, Object> busInfo = actExtendDao.queryBusiByBusId(params);
    Method[] methods = clazz.getDeclaredMethods();
    // 读取需要判断的条件字段，做为流程变量
    Map<String, Object> variables = new HashMap<String, Object>();
    for (Method method : methods) {
      ActField actField = method.getAnnotation(ActField.class);
      if (actField != null && actField.isJudg()) {
        String flidName = method.getName().replace("get", "");
        variables.put(flidName, busInfo.get(flidName));
      }
    }
    // 启动流程并设置启动变量（条件变量）
    ProcessInstance processInstance =
        runtimeService.startProcessInstanceById(
            processTaskDto.getDefId().getDefid(), processTaskDto.getBusId(), variables);
    processTaskDto.setInstanceId(processInstance.getId());
    // 更新当前业务表
    LocalDateTime curentTime = LocalDateTime.now();
    Map<String, Object> busParams = new HashMap<>();
    busParams.put("instanceId", processInstance.getId());
    busParams.put("defid", processTaskDto.getDefId().getDefid());
    busParams.put("startUserId", AuthUtils.getUser().getId());
    busParams.put("status", ActStauts.APPROVAL.getValue());
    busParams.put("startTime", curentTime);
    busParams.put(TableInfo.TAB_TABLENAME, actTable.tableName());
    busParams.put(TableInfo.TAB_PKNAME, actTable.pkName());
    busParams.put(TableInfo.TAB_ID, processTaskDto.getBusId());
    actExtendDao.updateBusInfo(busParams);
    // 保存任务日志表
    ExtendActTaskLog tasklog = new ExtendActTaskLog();
    tasklog.setFakeId(Utils.uuid());
    tasklog.setBusId(processTaskDto.getBusId());
    tasklog.setDefId(processTaskDto.getDefId().getDefid());
    tasklog.setInstanceId(processTaskDto.getInstanceId());
    tasklog.setTaskName("提交");
    tasklog.setDealTime(curentTime);
    tasklog.setDealId(AuthUtils.getUser());
    tasklog.setAppAction(ActTaskResult.AGREE);
    tasklog.setAppOpinion(processTaskDto.getRemark());
    // 保存流程业务关系表
    ExtendActFlowBus flowBus = new ExtendActFlowBus();
    flowBus.setBusId(processTaskDto.getBusId());
    flowBus.setDefid(processTaskDto.getDefId().getDefid());
    flowBus.setInstanceId(processTaskDto.getInstanceId());
    flowBus.setStartTime(curentTime);
    flowBus.setActKey(processTaskDto.getActKey());
    flowBus.setCode((String) busInfo.get("code"));
    flowBus.setStatus(ActStauts.APPROVAL);
    flowBus.setStartUserId(AuthUtils.getUser());
    flowBus.setTableName(actTable.tableName());
    flowbusService.save(flowBus);
    // 代理人设置 待完善
    // TODO: 2017/8/4  代理人设置 待完善
    // 如果第一个节点是审批节点
    if (Constant.NodeType.EXAMINE.getValue().equals(processTaskDto.getNodeType())) {
      List<org.activiti.engine.task.Task> tasks =
          taskService
              .createTaskQuery()
              .processDefinitionId(processTaskDto.getDefId().getDefid())
              .processInstanceId(processTaskDto.getInstanceId())
              .list();
      for (Task task : tasks) {
        // 设置下一个任务处理人
        taskService.setAssignee(
            task.getId(), String.valueOf(processTaskDto.getNextUserIds().getId()));
        // 记录任务日志
        ExtendActTaskLog tasklogEntity = new ExtendActTaskLog();
        tasklogEntity.setFakeId(Utils.uuid());
        tasklogEntity.setBusId(processTaskDto.getBusId());
        tasklogEntity.setDefId(processTaskDto.getDefId().getDefid());
        tasklogEntity.setInstanceId(processTaskDto.getInstanceId());
        tasklogEntity.setTaskId(task.getId());
        tasklogEntity.setTaskName(task.getName());
        tasklogEntity.setAdvanceId(processTaskDto.getNextUserIds());
        tasklogEntity.setCreateTime(DateConverUtil.dateToLocalDateTime(task.getCreateTime()));
        tasklogService.save(tasklogEntity);
      }
      // 提交之后，更改业务审批状态为审批中，审批结果也为审批中
      Map<String, Object> updateMap = new HashMap<>();
      updateMap.put(TableInfo.TAB_TABLENAME, actTable.tableName());
      updateMap.put(TableInfo.TAB_PKNAME, actTable.pkName());
      updateMap.put(TableInfo.TAB_ID, processTaskDto.getBusId());
      updateMap.put("status", ActStauts.APPROVAL.getValue());
      updateMap.put("actResult", ActResult.APPROVAL);
      actExtendDao.updateBusInfo(updateMap);
      // 如果第一个节点是结束节点 也就是空流程
    } else if (Constant.NodeType.END.getValue().equals(processTaskDto.getNodeType())) {
      tasklog.setAppOpinion("空流程结束");
      // 流程完成后，更改当前业务表的流程信息
      busParams.put("status", ActStauts.END.getValue());
      busParams.put("actResult", ActResult.AGREE);
      busParams.put(TableInfo.TAB_TABLENAME, actTable.tableName());
      busParams.put(TableInfo.TAB_PKNAME, actTable.pkName());
      busParams.put(TableInfo.TAB_ID, processTaskDto.getBusId());
      actExtendDao.updateBusInfo(busParams);
      // 更新流程业务关系表
      ExtendActFlowBus flowbusEntity = new ExtendActFlowBus();
      flowbusEntity.setBusId(processTaskDto.getBusId());
      flowbusEntity.setStatus(ActStauts.END);
      flowbusService.updateByBusId(flowbusEntity);
      // 获取下一个人工任务节点，进行回调执行
      ProcessDefinitionEntity processDefinitionEntity =
          (ProcessDefinitionEntity)
              ((RepositoryServiceImpl) repositoryService)
                  .getDeployedProcessDefinition(processTaskDto.getDefId().getDefid());
      // 获取流程定义的全部节点
      List<ActivityImpl> activities = processDefinitionEntity.getActivities();
      for (ActivityImpl node : activities) {
        // 节点类型为结束节点
        String type = (String) node.getProperty("type");
        if ("endEvent".equals(type)) {
          // 查询流程配置的回调函数
          ExtendActNodeSet nodesetEntity = nodesetService.queryByNodeId(node.getId());
          // 如果该节点配置有回调函数，则执行回调
          if (nodesetEntity != null && StringUtils.isNotEmpty(nodesetEntity.getCallBack())) {
            if (StringUtils.isNotEmpty(nodesetEntity.getCallBack())) {
              executeCallback(nodesetEntity.getCallBack(), processTaskDto);
            }
          }
          // 流程结束发送通知 待完善
          // TODO: 2017/8/4 流程结束发送通知 待完善
        }
      }
    } else {
      throw new MyException("流程设计错误!");
    }
    // 保存扩展任务日志
    tasklogService.save(tasklog);
  }

  /*
   * @Author GuoHongKai
   * @Description 查找节点详情
   * @Date 13:57 2020/8/19
   * @Param [nodeId, modelId]
   * @return void
   **/
  @Override
  public ExtendActNodeSet getNodeByNodeId(String nodeId, String modelId) {
    ExtendActNodeSet nodeSet = nodeSetRepository.findByNodeIdModelId(nodeId, modelId);
    return nodeSet;
  }

  /*
   * @Author GuoHongKai
   * @Description 查找我的待办列表
   * @Date 17:40 2020/8/19
   * @Param [params]
   * @return void
   **/
  @Override
  public void findMyUpcomingPage(Map<String, Object> params) {
    actExtendDao.findMyUpcomingPage(params);
  }

  /*
   * @Author GuoHongKai
   * @Description 查询需要作为流程条件判断的字段
   * @Date 9:41 2020/8/21
   * @Param [taskDefinitionKey, defid]
   * @return java.util.Set<java.lang.String>
   **/
  @Override
  public Set<String> getNextVarNams(String nodeId, String defId) {
    if (StringUtils.isEmpty(defId)) {
      throw new MyException("流程定义不能为空!");
    }
    if (StringUtils.isEmpty(nodeId)) {
      throw new MyException("流程节点不能为空!");
    }
    List<PvmTransition> nextPvmTransitions = actUtils.getNextPvmTransitions(defId, nodeId);
    List<String> nextIds = new ArrayList<>();
    for (PvmTransition pvmTransition : nextPvmTransitions) {
      nextIds.add(pvmTransition.getId());
    }
    if (nextIds.size() < 1) {
      return null;
    }
    List<ExtendActNodeField> nodeFields = fieldService.queryByNodes(nextIds);
    // 节点可更改的变量字段数组
    Set<String> vars = new HashSet<>();
    for (ExtendActNodeField nodefield : nodeFields) {
      vars.add(nodefield.getFieldName());
    }
    return vars;
  }

  /*
   * @Author GuoHongKai
   * @Description 办理任务
   * @Date 15:20 2020/8/21
   * @Param [processTaskDto, params]
   * @return void
   **/
  @Override
  @Transactional(rollbackOn = Exception.class)
  public void doActTask(ProcessTaskDto processTaskDto, Map<String, Object> params)
      throws Exception {
    if (StringUtils.isEmpty(processTaskDto.getTaskId())) {
      throw new MyException("流程任务id不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getInstanceId())) {
      throw new MyException("流程实例id不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getDefId().getDefid())) {
      throw new MyException("流程定义id不能为空");
    }
    if (StringUtils.isEmpty(processTaskDto.getBusId())) {
      throw new MyException("业务id不能为空");
    }
    // 根据流程定义id查询流程定义key
    ProcessDefinition processDefinition =
        repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionId(processTaskDto.getDefId().getDefid())
            .singleResult();
    // 保存审批信息
    Task task = taskService.createTaskQuery().taskId(processTaskDto.getTaskId()).singleResult();
    String remark = "";
    if (StringUtils.isNotEmpty(processTaskDto.getRemark())) {
      remark = processTaskDto.getRemark();
      taskService.addComment(processTaskDto.getTaskId(), processTaskDto.getInstanceId(), remark);
    }
    // 查询流程业务关联信息
    ExtendActModel businessEntity = businessService.queryByActKey(processDefinition.getKey());
    ExtendActNodeSet nodesetEntity = nodesetService.queryByNodeId(task.getTaskDefinitionKey());
    Class<?> clazz = Class.forName(businessEntity.getClassEntityStr().getFullName());
    ActTable actTable = clazz.getAnnotation(ActTable.class);
    // 更改的业务字段文本描述
    String filedText = "";
    // 更改的值不为空
    if (StringUtils.isNotEmpty(nodesetEntity.getChangeFiles())) {
      // 保存流程更改过的业务记录信息
      filedText =
          changeFields(
              actTable,
              processTaskDto.getBusId(),
              nodesetEntity.getChangeFiles(),
              businessEntity.getClassEntityStr().getFullName(),
              params);
    }
    // 流程变量
    Map<String, Object> elMap = new HashMap<>();
    // 获取流程变量
    if (StringUtils.isNotEmpty(processTaskDto.getVarName())) {
      String[] varNames = processTaskDto.getVarName().split(",");
      String[] varValues = processTaskDto.getVarValue().split(",");
      for (int i = 0; i < varNames.length; i++) {
        if (StringUtils.isEmpty(varNames[i])) {
          continue;
        }
        elMap.put(varNames[i], varValues[i]);
      }
    }
    // 获取下个节点信息
    List<PvmActivity> pvmActivities =
        actUtils.getNextActNodes(
            processTaskDto.getDefId().getDefid(), task.getTaskDefinitionKey(), elMap);
    for (PvmActivity pvmActivity : pvmActivities) {
      // 下一节点为结束节点时，完成任务更新业务表
      if ("endEvent".equals(pvmActivity.getProperty("type"))) {
        // 查询结束节点信息（主要查询回调）
        ExtendActNodeSet endNodeSet = nodesetService.queryByNodeId(pvmActivity.getId());
        // 提交任务设置流程变量
        taskService.complete(task.getId(), elMap);
        // 当前节点为会签节点时
        if (Constant.ActAction.MULIT.getValue().equals(nodesetEntity.getNodeAction().getValue())) {
          // 检测任务是否全部提交完
          // 会签是否全部执行完
          boolean isAllCompleted = true;
          // 查询实例中的任务是否全部提交完
          List<HistoricTaskInstance> historicTaskInstances =
              historyService
                  .createHistoricTaskInstanceQuery()
                  .processInstanceId(task.getProcessInstanceId())
                  .list();
          for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            if (historicTaskInstance.getEndTime() == null) {
              isAllCompleted = false;
              break;
            }
          }
          // 全部执行完，修改业务表
          if (isAllCompleted) {
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(TableInfo.TAB_TABLENAME, actTable.tableName());
            updateMap.put(TableInfo.TAB_PKNAME, actTable.pkName());
            updateMap.put(TableInfo.TAB_ID, processTaskDto.getBusId());
            updateMap.put("status", ActStauts.END.getValue());
            updateMap.put("actResult", ActResult.AGREE);
            actExtendDao.updateBusInfo(updateMap);
            ExtendActFlowBus flowbusEntity = new ExtendActFlowBus();
            flowbusEntity.setStatus(ActStauts.END);
            flowbusEntity.setBusId(processTaskDto.getBusId());
            flowbusService.updateByBusId(flowbusEntity);
            // 会签节点结束后,执行当前节点上的回调
            if (StringUtils.isNotEmpty(nodesetEntity.getCallBack())) {
              executeCallback(nodesetEntity.getCallBack(), processTaskDto);
            }
            // 执行结束节点回调
            if (StringUtils.isNotEmpty(endNodeSet.getCallBack())) {
              executeCallback(endNodeSet.getCallBack(), processTaskDto);
            }
          }
        } else if (Constant.ActAction.APPROVE
            .getValue()
            .equals(nodesetEntity.getNodeAction().getValue())) {
          // 当前节点为普通审批节点
          Map<String, Object> updateMap = new HashMap<>();
          updateMap.put(TableInfo.TAB_TABLENAME, actTable.tableName());
          updateMap.put(TableInfo.TAB_PKNAME, actTable.pkName());
          updateMap.put(TableInfo.TAB_ID, processTaskDto.getBusId());
          updateMap.put("status", ActStauts.END.getValue());
          updateMap.put("actResult", ActResult.AGREE);
          actExtendDao.updateBusInfo(updateMap);
          ExtendActFlowBus flowbusEntity = new ExtendActFlowBus();
          flowbusEntity.setStatus(ActStauts.END);
          flowbusEntity.setBusId(processTaskDto.getBusId());
          flowbusService.updateByBusId(flowbusEntity);
          // 执行当前节点上的回调
          if (StringUtils.isNotEmpty(nodesetEntity.getCallBack())) {
            executeCallback(nodesetEntity.getCallBack(), processTaskDto);
          }
          // 执行结束节点回调
          if (StringUtils.isNotEmpty(endNodeSet.getCallBack())) {
            executeCallback(endNodeSet.getCallBack(), processTaskDto);
          }
        }
        // 流程结束可以在这里写一些通知信息
        ExtendActFlowBus flowBus =
            flowbusService.queryByBusIdInsId(
                processTaskDto.getInstanceId(), processTaskDto.getBusId());
        sendNoticeMsg(String.valueOf(flowBus.getStartUserId().getId()), businessEntity);
      } else {
        // 下一个节点不为结束节点
        // 查询下个节点信息
        ExtendActNodeSet nextNode = nodesetService.queryByNodeId((String) pvmActivity.getId());
        // 下一级节点为会签节点
        if (Constant.ActAction.MULIT.getValue().equals(nextNode.getNodeAction().getValue())) {
          // TODO: 2017/8/10 暂不支持当前节点为会签，下一级节点也为会签
          // 设置会签人员集
          String[] nextUsers = String.valueOf(processTaskDto.getNextUserIds().getId()).split(",");
          Map<String, Object> userMap = new HashMap<>();
          userMap.put(Constant.ACT_MUIT_LIST_NAME, Arrays.asList(nextUsers));
          userMap.putAll(elMap);
          // 完成任务并设置流程变量
          taskService.complete(task.getId(), userMap);
          // 查询流程所有任务
          List<Task> taskList =
              taskService
                  .createTaskQuery()
                  .processInstanceId(processTaskDto.getInstanceId())
                  .list();
          for (Task t : taskList) {
            // 记录任务日志
            ExtendActTaskLog tasklogEntity = new ExtendActTaskLog();
            tasklogEntity.setFakeId(Utils.uuid());
            tasklogEntity.setBusId(processTaskDto.getBusId());
            tasklogEntity.setDefId(processTaskDto.getDefId().getDefid());
            tasklogEntity.setInstanceId(processTaskDto.getInstanceId());
            tasklogEntity.setTaskId(t.getId());
            tasklogEntity.setTaskName(t.getName());
            tasklogEntity.setAdvanceId(UserUtils.getUserByUserId(Long.valueOf(t.getAssignee())));
            tasklogEntity.setCreateTime(DateConverUtil.dateToLocalDateTime(task.getCreateTime()));
            tasklogService.save(tasklogEntity);
          }
          // 下级节点为普通审批节点
        } else if (Constant.ActAction.APPROVE
            .getValue()
            .equals(nextNode.getNodeAction().getValue())) {
          // 完成任务
          taskService.complete(task.getId(), elMap);
          // 会签是否结束
          boolean isOver = true;
          // 当前节点为会签节点
          if (Constant.ActAction.MULIT
              .getValue()
              .equals(nodesetEntity.getNodeAction().getValue())) {
            List<Task> nodeTasks =
                taskService
                    .createTaskQuery()
                    .taskDefinitionKey(nodesetEntity.getNodeId())
                    .processInstanceId(processTaskDto.getInstanceId())
                    .list();
            if (nodeTasks.size() > 0) {
              isOver = false;
            }
          }
          if (isOver) {
            // 如果会签已经完成，则记录下一任务日志
            List<Task> tasks =
                taskService
                    .createTaskQuery()
                    .processInstanceId(processTaskDto.getInstanceId())
                    .list();
            for (Task t : tasks) {
              // 设置下一个任务的办理人
              // TODO: 2017/8/10 如果是下个节点是并行结果，那么这里需要处理下 待开发
              taskService.setAssignee(
                  t.getId(), String.valueOf(processTaskDto.getNextUserIds().getId()));
              ExtendActTaskLog tasklogEntity = new ExtendActTaskLog();
              tasklogEntity.setFakeId(Utils.uuid());
              tasklogEntity.setBusId(processTaskDto.getBusId());
              tasklogEntity.setDefId(processTaskDto.getDefId().getDefid());
              tasklogEntity.setInstanceId(processTaskDto.getInstanceId());
              tasklogEntity.setTaskId(t.getId());
              tasklogEntity.setTaskName(t.getName());
              tasklogEntity.setAdvanceId(processTaskDto.getNextUserIds());
              tasklogEntity.setCreateTime(DateConverUtil.dateToLocalDateTime(task.getCreateTime()));
              tasklogService.save(tasklogEntity);
            }
          }
        }
      }
    }
    // 处理任务后，更新任务日志
    ExtendActTaskLog tasklogEntity = tasklogService.findTaskLogByTaskId(task.getId());
    tasklogEntity.setTaskId(task.getId());
    tasklogEntity.setDealTime(LocalDateTime.now());
    tasklogEntity.setAppOpinion(remark);
    tasklogEntity.setDealId(AuthUtils.getUser());
    tasklogEntity.setColumns(filedText.toString());
    tasklogEntity.setAppAction(ActTaskResult.AGREE);
    int i = tasklogService.updateByTaskId(tasklogEntity);
    if (i < 1) {
      throw new MyException("更新任务日志失败");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 我的已办
   * @Date 15:47 2020/8/22
   * @Param [params]
   * @return void
   **/
  @Override
  public void myDonePage(Map<String, Object> params) {
    // 超级管理员可查看所有待办
    if ("admin".equals(AuthUtils.getUser().getCode())) {
      params.put("dealId", AuthUtils.getUser().getId());
    }
    List<ProcessTaskDto> myDoneList = actExtendDao.findMyDoneList(params);
    System.out.println(myDoneList);
  }

  /*
   * @Author GuoHongKai
   * @Description 办理任务时，获取流程下一流向节点集合
   * @Date 15:13 2020/8/26
   * @Param [processTaskDto]
   * @return java.util.List<com.hypaas.activiti.entity.ProcessNodeDto>
   **/
  @Override
  public List<ProcessNodeDto> getNextActNodes(ProcessTaskDto processTaskDto) {
    if (StringUtils.isEmpty(processTaskDto.getDefId().getDefid())) {
      throw new MyException("流程定义id不能为空!");
    }
    if (StringUtils.isEmpty(processTaskDto.getTaskId())) {
      throw new MyException("流程任务id不能为空!");
    }
    Task task = taskService.createTaskQuery().taskId(processTaskDto.getTaskId()).singleResult();
    String[] varNames = processTaskDto.getVarName().split(",");
    String[] varValues = processTaskDto.getVarValue().split(",");
    Map<String, Object> elMap = new HashMap<>();
    for (int i = 0; i < varNames.length; i++) {
      if (StringUtils.isEmpty(varNames[i])) {
        continue;
      }
      elMap.put(varNames[i], varValues[i]);
    }
    List<PvmActivity> pvmActivities =
        actUtils.getNextActNodes(
            processTaskDto.getDefId().getDefid(), task.getTaskDefinitionKey(), elMap);
    List<ProcessNodeDto> listNode = new ArrayList<>();
    ProcessNodeDto processNodeDto = null;
    for (PvmActivity pvm : pvmActivities) {
      processNodeDto = new ProcessNodeDto();
      processNodeDto.setNodeId(pvm.getId());
      processNodeDto.setNodeName((String) pvm.getProperty("name"));
      ExtendActNodeSet nodeSet = nodesetService.queryByNodeId(pvm.getId());
      //      processNodeDto.setNodeTypeName(nodeSet.getNodeType().getValue());
      //      processNodeDto.setNodeActionName(nodeSet.getNodeAction().getValue());
      processNodeDto.setNodeAction(nodeSet.getNodeAction().getValue());
      processNodeDto.setNodeType(nodeSet.getNodeType().getValue());
      listNode.add(processNodeDto);
    }
    return listNode;
  }

  /*
   * @Author GuoHongKai
   * @Description 发送待办消息，暂时不添加
   * @Date 11:20 2020/8/22
   * @Param [startUserId, businessEntity]
   * @return void
   **/
  private void sendNoticeMsg(String startUserId, ExtendActModel businessEntity) {
    //    NoticeEntity noticeEntity = new NoticeEntity();
    //    noticeEntity.setTitle("流程通知【"+businessEntity.getName()+"】");
    //    noticeEntity.setContext("亲，你提交的流程【"+businessEntity.getName()+"】已经审批结束了,请查阅对应模块！");
    //    noticeEntity.setCreateTime(new Date());
    //    noticeEntity.setIsUrgent(Constant.YESNO.YES.getValue());
    //    noticeEntity.setReleaseTimee(new Date());
    //    noticeEntity.setSoucre(Constant.noticeType.ACT_NOTICE.getValue());
    //    noticeEntity.setId(Utils.uuid());
    //    noticeEntity.setStatus(Constant.YESNO.YES.getValue());
    //    NoticeUserEntity noticeUserEntity = new NoticeUserEntity();
    //    noticeUserEntity.setId(Utils.uuid());
    //    noticeUserEntity.setNoticeId(noticeEntity.getId());
    //    noticeUserEntity.setStatus(Constant.YESNO.NO.getValue());
    //    noticeUserEntity.setUserId(userId);
    //    noticeService.save(noticeEntity);
    //    noticeUserDao.save(noticeUserEntity);
  }

  /*
   * @Author GuoHongKai
   * @Description 保存流程更改过的业务记录信息
   * @Date 15:23 2020/8/21
   * @Param [actTable, busId, changeFiles, classurl, params]
   * @return java.lang.String
   **/
  private String changeFields(
      ActTable actTable,
      String busId,
      String changeFieldNames,
      String classUrl,
      Map<String, Object> map) {
    if (StringUtils.isEmpty(changeFieldNames)) {
      return "";
    }
    // 更改的业务字段文本描述
    StringBuffer filedText = new StringBuffer();
    // 查询业务记录
    Map<String, Object> tamap = new HashMap<>();
    tamap.put(TableInfo.TAB_TABLENAME, actTable.tableName());
    tamap.put(TableInfo.TAB_PKNAME, actTable.pkName());
    tamap.put(TableInfo.TAB_ID, busId);
    Map<String, Object> busInfo = actExtendDao.queryBusiByBusId(tamap);
    Map<String, String> textMap = new HashMap<>();
    List<Map<String, Object>> mapList = AnnotationUtils.getActFieldByClazz(classUrl);
    for (Map remap : mapList) {
      ActField actField = (ActField) remap.get("actField");
      String keyName = (String) remap.get("keyName");
      if (actField != null) {
        textMap.put(keyName, actField.name());
      }
    }
    // 业务可更改的字段
    String[] changeFields = changeFieldNames.split(",");
    // 业务可更改的字段和对应更改后的值
    List<TableInfo> fields = new ArrayList<>();
    for (int i = 0; i < changeFields.length; i++) {
      if (StringUtils.isEmpty(changeFields[i])) {
        continue;
      }
      // 原值
      Object o = busInfo.get(changeFields[i]);
      // 更改后的值
      Object o1 = map.get(changeFields[i]);
      // 字段text 例：请假天数
      String text = textMap.get(changeFields[i]);
      if (o instanceof String) {
        if (!o.equals(o1)) {
          filedText.append(text + "的原值【" + o + "】,更改后【" + o1 + "】;");
        }
      } else if (o instanceof Integer) {
        if (((Integer) o).intValue() != Integer.parseInt(((String) o1))) {
          filedText.append(text + "的原值为【" + o + "】,更改为【" + o1 + "】;");
        }
      }
      fields.add(new TableInfo(changeFields[i], map.get(changeFields[i])));
    }
    // 保存业务更改后的值
    Map<String, Object> params = new HashMap<>();
    params.put(TableInfo.TAB_TABLENAME, actTable.tableName());
    params.put(TableInfo.TAB_PKNAME, actTable.pkName());
    params.put(TableInfo.TAB_ID, busId);
    params.put(TableInfo.TAB_FIELDS, fields);
    int count = actExtendDao.updateChangeBusInfo(params);
    if (count < 1) {
      throw new MyException("更新业务信息失败");
    }
    return filedText.toString();
  }

  /**
   * 节点回调方法 执行
   *
   * @param callBack
   * @param processTaskDto
   * @throws Exception
   */
  public void executeCallback(String callBack, ProcessTaskDto processTaskDto) throws Exception {
    int lastIndex = callBack.lastIndexOf(".");
    String methodStr = callBack.substring(lastIndex + 1); // 方法名
    String classUrl = callBack.substring(0, lastIndex); // 类路径
    Class<?> clazz = Class.forName(classUrl);
    Object o = clazz.newInstance();
    // 回调方法参数 这里可扩展
    Method method = clazz.getMethod(methodStr, ProcessTaskDto.class);
    method.invoke(o, processTaskDto);
  }
}
