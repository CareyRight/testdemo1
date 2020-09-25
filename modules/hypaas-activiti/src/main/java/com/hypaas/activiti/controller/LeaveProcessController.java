package com.hypaas.activiti.controller;

import com.google.inject.Inject;
import com.hypaas.activiti.db.*;
import com.hypaas.activiti.service.ActModelerService;
import com.hypaas.activiti.service.LeaveProcessService;
import com.hypaas.activiti.utils.Utils;
import com.hypaas.auth.AuthUtils;
import com.hypaas.auth.db.User;
import com.hypaas.i18n.I18n;
import com.hypaas.meta.schema.actions.ActionView;
import com.hypaas.rpc.ActionRequest;
import com.hypaas.rpc.ActionResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName LeaveProcessController @Description TODO @Author GuoHongKai @Date 2020/8/14
 * 16:42 @Version 1.0
 */
public class LeaveProcessController {

  @Inject private LeaveProcessService leaveService;
  @Inject private ActModelerService actModelerService;
  @Inject private ExtendActDealController actDealController;

  /*
   * @Author GuoHongKai
   * @Description 申请人为当前用户
   * @Date 14:24 2020/8/14
   * @Param [request, response]
   * @return void
   **/
  public void setDefaultApplicant(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    response.setValue("applicant", user);
    response.setValue("status", ActStauts.DRAFT.getValue());
    response.setValue("code", Utils.getCode("D"));
    response.setValue("busId", Utils.uuid());
  }

  /*
   * @Author GuoHongKai
   * @Description 校验当前审批单的状态,不可重复提交审批
   * @Date 16:19 2020/8/14
   * @Param [request, response]
   * @return void
   **/
  public void validateActstauts(ActionRequest request, ActionResponse response) {
    Leave leave = request.getContext().asType(Leave.class);
    ActStauts status = leave.getStatus();
    if (status != ActStauts.DRAFT && status != null) {
      response.setError("The submitted approval form cannot be modified!");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 请假单删除
   * @Date 16:44 2020/8/14
   * @Param [request, response]
   * @return void
   **/
  public void leaveDelete(ActionRequest request, ActionResponse response) {
    Leave leave = request.getContext().asType(Leave.class);
    try {
      leaveService.leaveDelete(leave.getId());
      response.setReload(true);
      response.setFlash("单据删除成功!");
    } catch (Exception e) {
      e.printStackTrace();
      response.setFlash("单据删除失败!");
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 提交到通用页面选择流程和候选人
   * @Date 16:44 2020/8/14
   * @Param [request, response]
   * @return void
   **/
  public void leaveSubmit(ActionRequest request, ActionResponse response) throws IOException {
    Leave leave = request.getContext().asType(Leave.class);
    String actKey =
        request.getModel().substring(request.getModel().lastIndexOf(".") + 1).toLowerCase();

    // 根据actKey查询可用的流程定义,activiti引擎提供
    try {
      List<Map<String, Object>> flows = actDealController.queryFlowsByActKey(actKey);
      // 不可取
      actModelerService.deleteAllFlows();
      actModelerService.addAllFlows(flows, actKey);
    } catch (Exception e) {
      e.printStackTrace();
      response.setError("没有可用的流程，请先定义!");
    }
    response.setView(
        ActionView.define(I18n.get("Process Submit"))
            .model(ProcessTaskDto.class.getName())
            .add("form", "process-task-dto-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("width", "500")
            .param("popup-save", "false")
            .param("show-confirm", "false")
            .context("_startUser", leave.getApplicant())
            .context("_status", leave.getStatus())
            .context("_busId", leave.getBusId())
            .map());
  }
}
