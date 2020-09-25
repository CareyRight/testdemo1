package com.hypaas.activiti.controller;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.hypaas.activiti.common.Constant;
import com.hypaas.activiti.db.*;
import com.hypaas.activiti.db.repo.ExtendActNodeSetRepository;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.mapper.MetaModelMapper;
import com.hypaas.activiti.service.*;
import com.hypaas.activiti.utils.StringTool;
import com.hypaas.db.ValueEnum;
import com.hypaas.i18n.I18n;
import com.hypaas.meta.db.MetaModel;
import com.hypaas.meta.schema.actions.ActionView;
import com.hypaas.rpc.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

/**
 * @ClassName ExtendActModelController @Description 流程模型相关操作 @Author GuoHongKai @Date 2020/8/6
 * 15:28 @Version 1.0
 */
public class ExtendActModelController {

  @Inject private ExtendActModelerService extendActModelService;
  @Inject private ActModelerService actModelerService;
  @Inject private ExtendActBusinessService businessService;
  @Inject private ExtendActNodesetService nodesetService;
  @Inject private ExtendActNodeUserService nodeuserService;
  @Inject private ExtendActNodeFieldService fieldService;
  @Inject private ExtendActNodeSetRepository nodeSetRepository;

  /*
   * @Author GuoHongKai
   * @Description 保存流程定义模型
   * @Date 9:06 2020/8/11
   * @Param [request, response]
   * @return com.hypaas.rpc.Response
   **/
  public void save(ActionRequest request, ActionResponse response) {
    ExtendActModel actModel = request.getContext().asType(ExtendActModel.class);
    String name = actModel.getClassEntityStr().getName();
    actModel.setActKey(name.toLowerCase());
    System.out.println(actModel);
    try {
      String modelId = extendActModelService.save(actModel);
    } catch (Exception e) {
      e.printStackTrace();
      response.setAlert(e.getMessage());
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 获取流程图所有节点和连线
   * @Date 10:09 2020/8/10
   * @Param [modelId, model]
   * @return java.lang.String
   **/
  @Transactional
  public void flowTree(ActionRequest request, ActionResponse response) throws Exception {
    ExtendActModel actModel = request.getContext().asType(ExtendActModel.class);
    System.out.println(actModel);
    // 所有节点
    List<Map<String, String>> flows =
        actModelerService.getflows(String.valueOf(actModel.getModelId()));
    // 所有回调和业务相关设置
    // ExtendActBusinessEntity businessEntity =
    // businessService.queryActBusByModelId(String.valueOf(actModel.getModelId()));

    // 方法不可取
    List<ExtendActNodeSet> fetch =
        nodeSetRepository.findNodeSetByModelId(actModel.getModelId()).fetch();
    if (fetch == null || fetch.size() == 0) {
      for (int i = 0; i < flows.size(); i++) {
        ExtendActNodeSet node = new ExtendActNodeSet();
        node.setModelId(flows.get(i).get("modelId"));
        node.setNodeId(flows.get(i).get("treeId"));
        node.setNodeType(ValueEnum.of(NodeType.class, flows.get(i).get("type")));
        if ("5".equals(flows.get(i).get("type"))) { // 结束节点也是审批动作
          node.setNodeAction(NodeAction.ACTION_APPROVA);
        }
        node.setNodeName(flows.get(i).get("treeName"));
        nodeSetRepository.save(node);
      }
    }
    response.setView(
        ActionView.define(I18n.get("Node Settings"))
            .model(ExtendActNodeSet.class.getName())
            .add("grid", "ExtendActNodeSet-grid")
            .add("form", "ExtendActNodeSet-form")
            .param("details-view", "true")
            .domain("self.modelId=" + actModel.getModelId())
            .map());
  }

  /*
   * @Author GuoHongKai
   * @Description 获取节点的扩展设置信息，暂时无用
   * @Date 15:21 2020/8/10
   * @Param [nodeId, type]
   * @return com.hypaas.rpc.Response
   **/
  public void flowSetInfo(ActionRequest request, ActionResponse response) {
    ExtendActNodeSet flows = request.getContext().asType(ExtendActNodeSet.class);
    String nodeId = flows.getNodeId();
    NodeType type = flows.getNodeType();
    if (StringUtils.isEmpty(nodeId)) {
      throw new MyException("未获取节点id");
    }
    //    Result result = new Result();
    ExtendActNodeSet query = new ExtendActNodeSet();
    query.setNodeId(nodeId);
    ExtendActNodeSet nodesetEntity = nodesetService.queryByNodeId(nodeId);
    //    result.put("nodeSet",nodesetEntity);
    // 如果节点类型为审批节点
    if (Constant.NodeType.EXAMINE.getValue().equals(type)) {
      List<ExtendActNodeUser> userLists = nodeuserService.getNodeUserByNodeId(nodeId);
      System.out.println(userLists);
      //      result.put("userList",userLists);
    }
    // 节点类型为连线
    if (Constant.NodeType.LINE.getValue().equals(type)) {
      Map<String, Object> params = new HashMap<>();
      params.put("nodeId", nodeId);
      //            params.put("fieldType","2");
      List<ExtendActNodeField> fields = fieldService.queryList(params);
      //      result.put("fields",fields);
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 查看流程定义图
   * @Date 15:44 2020/8/10
   * @Param [modelId]
   * @return org.springframework.http.ResponseEntity<byte[]>
   **/
  public void showFlowImg(ActionRequest request, ActionResponse response) {
    ExtendActModel actModel = request.getContext().asType(ExtendActModel.class);
    try {
      ResponseEntity<byte[]> responseEntity =
          actModelerService.showFlowImg(String.valueOf(actModel.getId()));
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash(e.getMessage());
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 删除模型
   * @Date 15:45 2020/8/10
   * @Param [id]
   * @return com.hypaas.rpc.Response
   **/
  public void del(ActionRequest request, ActionResponse response) {
    ExtendActModel actModel = request.getContext().asType(ExtendActModel.class);
    try {
      extendActModelService.delete(String.valueOf(actModel.getId()));
      response.setReload(true);
      response.setFlash("模型删除成功！");
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash(e.getMessage());
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 保存节点设置，暂时无用
   * @Date 15:50 2020/8/10
   * @Param [nodesetEntity]
   * @return Result
   **/
  public void saveNode(ActionRequest request, ActionResponse response) {
    ExtendActNodeSet actModel = request.getContext().asType(ExtendActNodeSet.class);
    try {
      ExtendActNodeSet nodeSet = nodesetService.saveNode(actModel);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 部署流程
   * @Date 16:09 2020/8/10
   * @Param [modelId]
   * @return com.hypaas.rpc.Response
   **/
  public void deploy(ActionRequest request, ActionResponse response) {
    try {
      ExtendActModel actModel = request.getContext().asType(ExtendActModel.class);
      extendActModelService.deploy(String.valueOf(actModel.getModelId()));
      response.setFlash("流程部署成功!");
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash("流程部署失败!");
    }
  }

  @Inject private MetaModelMapper modelMapper;
  /*
   * @Author GuoHongKai
   * @Description 选择审批流实体
   * @Date 13:31 2020/8/31
   * @Param []
   * @return void
   **/
  public void loadClassUrls(ActionRequest request, ActionResponse response) {
    List<MetaModel> list = modelMapper.findClassUrls();
    String idList = StringTool.getIdListString(list);
    response.setAttr("classEntityStr", "domain", "self.id IN (" + idList + ")");
  }
}
