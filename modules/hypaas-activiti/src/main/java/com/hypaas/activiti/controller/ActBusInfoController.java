package com.hypaas.activiti.controller;

import com.google.inject.Inject;
import com.hypaas.activiti.db.ExtendActFlowBus;
import com.hypaas.activiti.db.Leave;
import com.hypaas.activiti.db.ProcessTaskDto;
import com.hypaas.activiti.service.LeaveService;
import com.hypaas.i18n.I18n;
import com.hypaas.meta.schema.actions.ActionView;
import com.hypaas.rpc.ActionRequest;
import com.hypaas.rpc.ActionResponse;

/*
 * @Author GuoHongKai
 * @Description 类的功能描述. 流程相关的业务根据业务id查询公共类，路径为actKey，也就是业务key
 * @Date 10:50 2020/8/25
 * @Param
 * @return
 **/
public class ActBusInfoController {

  @Inject private LeaveService leaveService;

  /*
   * @Author GuoHongKai
   * @Description 查询详细业务信息
   * @Date 18:41 2020/8/19
   * @Param [request, response]
   * @return void
   **/
  public void leave(ActionRequest request, ActionResponse response) {
    ExtendActFlowBus flowBus = request.getContext().asType(ExtendActFlowBus.class);
    // 区分不同种类型的单据展示详情后续再考虑扩展性
    if ("leave".equals(flowBus.getActKey())) {
      Leave leave = leaveService.queryObject(flowBus.getBusId());
      response.setView(
          ActionView.define(I18n.get("Leaves"))
              .model(ProcessTaskDto.class.getName())
              .add("form", "leave-form-detail")
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("width", "500")
              .param("popup-save", "false")
              .param("show-confirm", "false")
              .context("_applicant", leave.getApplicant())
              .context("_title", leave.getTitle())
              .context("_num", leave.getNum())
              .context("_status", leave.getStatus())
              .context("_reson", leave.getReson())
              .context("_actResult", leave.getActResult())
              .map());
    } else {
      response.setError("展示不同类型的单据详情。。。");
    }
  }
}
