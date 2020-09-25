package com.hypaas.activiti.config;

import com.google.inject.name.Named;
import com.hypaas.event.Observes;
import com.hypaas.event.Priority;
import com.hypaas.events.PreRequest;
import com.hypaas.events.RequestEvent;
import com.hypaas.events.qualifiers.EntityType;
import com.hypaas.team.db.Team;

/**
 * @ClassName ContactObserver @Description TODO @Author GuoHongKai @Date 2020/8/31 9:44 @Version 1.0
 */
public class ContactObserver {
  void onExport(
      @Observes @Priority(0) @Named(RequestEvent.COPY) @EntityType(Team.class) PreRequest event) {

    System.out.println(event.getSource());
  }
}
