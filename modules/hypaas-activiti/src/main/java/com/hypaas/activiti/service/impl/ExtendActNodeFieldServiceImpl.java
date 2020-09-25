package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.hypaas.activiti.db.ExtendActNodeField;
import com.hypaas.activiti.mapper.ExtendActNodeFieldMapper;
import com.hypaas.activiti.service.ExtendActNodeFieldService;
import com.hypaas.db.JpaSupport;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * @ClassName ExtendActNodeFieldServiceImp @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:36 @Version 1.0
 */
public class ExtendActNodeFieldServiceImpl extends JpaSupport implements ExtendActNodeFieldService {

  @Inject private ExtendActNodeFieldMapper extendActNodefieldDao;

  /*
   * @Author GuoHongKai
   * @Description 节点类型为连线
   * @Date 10:09 2020/8/13
   * @Param [params]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeField>
   **/
  @Override
  public List<ExtendActNodeField> queryList(Map<String, Object> params) {
    EntityManager em = getEntityManager();
    Query query =
        em.createQuery(
            "select self from ExtendActNodeSet self where self.nodeId = '"
                + params.get("nodId")
                + "'");
    try {
      List<ExtendActNodeField> singleResult = (List<ExtendActNodeField>) query.getSingleResult();
      return singleResult;
    } catch (NoResultException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * @Author GuoHongKai
   * @Description 根据节点集合查询
   * @Date 9:50 2020/8/21
   * @Param [nextIds]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeField>
   **/
  @Override
  public List<ExtendActNodeField> queryByNodes(List<String> nextIds) {
    return extendActNodefieldDao.queryByNodes(nextIds);
  }
}
