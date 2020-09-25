package com.hypaas.activiti.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.db.ActModelStatus;
import com.hypaas.activiti.db.ExtendActModel;
import com.hypaas.activiti.db.repo.ExtendActModelRepository;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.service.ActModelerService;
import com.hypaas.activiti.service.ExtendActModelerService;
import com.hypaas.db.JpaSupport;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;

/**
 * @ClassName ExtendActModelServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/6
 * 17:02 @Version 1.0
 */
public class ExtendActModelerServiceImpl extends JpaSupport implements ExtendActModelerService {

  @Inject private ActModelerService actModelerService;
  @Inject private ExtendActModelRepository extendActModelRe;
  @Inject private RepositoryService repositoryService;
  @Inject private ObjectMapper objectMapper;

  /*
   * @Author GuoHongKai
   * @Description 保存流程模型
   * @Date 16:32 2020/8/24
   * @Param [actModel]
   * @return java.lang.String
   **/
  @Override
  @Transactional
  public String save(ExtendActModel actModel) throws Exception {

    return actModelerService.CreateModeler(actModel);
  }

  /*
   * @Author GuoHongKai
   * @Description 删除流程模型
   * @Date 16:32 2020/8/24
   * @Param [id]
   * @return void
   **/
  @Override
  @Transactional
  public void delete(String id) {
    ExtendActModel actmodel = extendActModelRe.find(Long.valueOf(id));
    Model model = repositoryService.getModel(actmodel.getModelId());
    if (!com.hypaas.activiti.utils.StringUtils.isEmpty(model.getDeploymentId())) {
      // 删除部署表
      repositoryService.deleteDeployment(model.getDeploymentId());
    }
    // 删除模型表
    repositoryService.deleteModel(model.getId());
    extendActModelRe.remove(actmodel);
  }

  /*
   * @Author GuoHongKai
   * @Description 部署流程模型
   * @Date 16:32 2020/8/24
   * @Param [modelId]
   * @return void
   **/
  @Override
  @Transactional
  public void deploy(String modelId) throws Exception {
    if (com.hypaas.activiti.utils.StringUtils.isEmpty(modelId)) {
      throw new MyException("流程模型id不能为空!");
    }
    Model model = repositoryService.getModel(modelId);
    // 读取editorSource
    JsonNode jsonNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelId));
    // 转换editorSource为bpmnModel
    BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
    // 获取流程名称
    List<Process> processes = bpmnModel.getProcesses();
    if (processes.size() == 0) {
      throw new MyException("没有设计流程图!");
    }
    Process process = processes.get(0);
    // 设置流程属性
    // ExtendActModel extModel = extendActModelRe.getModelAndBusInfo(modelId);
    // 根据模型id获取流程模型和业务相关信息
    EntityManager em = getEntityManager();
    Query q1 = em.createQuery("select m from ExtendActModel m where m.modelId =" + modelId);
    ExtendActModel extModel = (ExtendActModel) q1.getSingleResult();
    //        String key=StringUtils.toStringByObject(extModel.getActKey())+"_"+modelId;
    String key = com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getActKey());
    process.setId(key);
    process.setName(com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getName()));
    process.setDocumentation(
        com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getDescription()));
    ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
    // 更新模型信息
    repositoryService.addModelEditorSource(modelId, objectNode.toString().getBytes("utf-8"));
    // 更新模型
    model.setName(com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getName()));
    model.setKey(key);
    // metaInfo
    ObjectNode modelObjectNode = objectMapper.createObjectNode();
    modelObjectNode.put(
        ModelDataJsonConstants.MODEL_NAME,
        com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getName()));
    modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
    modelObjectNode.put(
        ModelDataJsonConstants.MODEL_DESCRIPTION,
        com.hypaas.activiti.utils.StringUtils.toStringByObject(extModel.getDescription()));
    model.setMetaInfo(modelObjectNode.toString());
    // 设置流程名称
    String deployName = process.getName();
    // 转换bpmnModel为可部署的xml形式
    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
    Deployment deployment =
        repositoryService
            .createDeployment()
            .name(deployName)
            .addString(modelId + ".bpmn20.xml", new String(bpmnBytes, "utf-8"))
            .deploy();
    // 更新模型部署id
    model.setDeploymentId(deployment.getId());
    repositoryService.saveModel(model);
    // 修改扩展模型状态
    extModel.setModelId(modelId);
    extModel.setStatus(ActModelStatus.PUBLISHED);
    extModel.setActVersion(model.getVersion());
    extendActModelRe.save(extModel); // update
  }
}
