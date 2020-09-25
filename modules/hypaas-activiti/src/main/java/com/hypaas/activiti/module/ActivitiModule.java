package com.hypaas.activiti.module;

import com.hypaas.activiti.config.ContactObserver;
import com.hypaas.activiti.service.*;
import com.hypaas.activiti.service.impl.*;
import com.hypaas.app.HypaasModule;

public class ActivitiModule extends HypaasModule {

  @Override
  protected void configure() {
    bind(ContactObserver.class);
    bind(ExtendActModelerService.class).to(ExtendActModelerServiceImpl.class);
    bind(ActModelerService.class).to(ActModelerServiceImpl.class);
    bind(ExtendActBusinessService.class).to(ExtendActBusinessServiceImpl.class);
    bind(ExtendActNodeFieldService.class).to(ExtendActNodeFieldServiceImpl.class);
    bind(ExtendActNodesetService.class).to(ExtendActNodesetServiceImpl.class);
    bind(ExtendActNodeUserService.class).to(ExtendActNodeUserServiceImpl.class);
    bind(LeaveProcessService.class).to(LeaveProcessServiceImpl.class);
    bind(ExtendActFlowbusService.class).to(ExtendActFlowbusServiceImpl.class);
    bind(ExtendActTasklogService.class).to(ExtendActTasklogServiceImpl.class);
    bind(LeaveService.class).to(LeaveServiceImpl.class);
  }
}
