package com.hypaas.activiti.service.impl;

import com.hypaas.activiti.db.ExtendActNodeUser;
import com.hypaas.activiti.service.ExtendActNodeUserService;
import com.hypaas.db.JpaSupport;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * @ClassName ExtendActNodeUserServiceImpl @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:34 @Version 1.0
 */
public class ExtendActNodeUserServiceImpl extends JpaSupport implements ExtendActNodeUserService {
  @Override
  public List<ExtendActNodeUser> getNodeUserByNodeId(String nodeId) {
    EntityManager em = getEntityManager();
    try {
      Query query =
          em.createQuery(
              "select self from ExtendActNodeUser self where self.nodeId = '" + nodeId + "'");
      List userList = query.getResultList();
      System.out.println(userList);
      return userList;
    } catch (NoResultException e) {
      e.printStackTrace();
      return null;
    }
  }
}
