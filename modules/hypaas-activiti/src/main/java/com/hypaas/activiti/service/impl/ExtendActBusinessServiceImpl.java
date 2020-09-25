package com.hypaas.activiti.service.impl;

import com.hypaas.activiti.db.ExtendActModel;
import com.hypaas.activiti.entity.ExtendActBusinessEntity;
import com.hypaas.activiti.service.ExtendActBusinessService;
import com.hypaas.activiti.utils.AnnotationUtils;
import com.hypaas.db.JpaSupport;
import com.hypaas.db.annotations.ActField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.BeanUtils;

/**
 * @ClassName ExtendActBusinessService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 10:20 @Version 1.0
 */
public class ExtendActBusinessServiceImpl extends JpaSupport implements ExtendActBusinessService {

  /*
   * @Author GuoHongKai
   * @Description 查询所有回调和业务相关设置
   * @Date 15:09 2020/8/11
   * @Param [modelId]
   * @return com.hypaas.activiti.db.ExtendActBusiness
   **/
  @Override
  public ExtendActBusinessEntity queryActBusByModelId(String modelId) {
    EntityManager em = getEntityManager();
    Query q1 = em.createQuery("SELECT m FROM ExtendActModel m WHERE m.modelId=" + modelId);
    ExtendActModel extendActBusinessEntity = (ExtendActModel) q1.getSingleResult();
    ExtendActBusinessEntity businessEntity = new ExtendActBusinessEntity();
    BeanUtils.copyProperties(extendActBusinessEntity, businessEntity);
    // 业务实体类
    List<Map<String, Object>> writes = new ArrayList<Map<String, Object>>(); // 可写
    List<Map<String, Object>> judgs = new ArrayList<>(); // 可设置为条件
    Map<String, Object> temMap = new HashMap();
    temMap.put("value", "isAgree");
    temMap.put("name", "是否通过");
    judgs.add(temMap);
    String classurl = "com.hypaas.activiti.entity.LeaveEntity";
    // writes.add(temMap);
    List<Map<String, Object>> mapList = AnnotationUtils.getActFieldByClazz(classurl);
    for (Map remap : mapList) {
      temMap = new HashMap();
      ActField actField = (ActField) remap.get("actField");
      String keyName = (String) remap.get("keyName");
      if (actField != null) {
        temMap.put("value", keyName);
        temMap.put("name", actField.name());
        writes.add(temMap);
        if (actField.isJudg()) {
          temMap.put("allow", actField.isJudg());
          judgs.add(temMap);
        }
      }
    }
    businessEntity.setJudgs(judgs);
    businessEntity.setWrites(writes);
    return businessEntity;
  }

  /*
   * @Author GuoHongKai
   * @Description 根据流程key查询业务定义
   * @Date 18:38 2020/8/18
   * @Param [actKey]
   * @return com.hypaas.activiti.entity.ExtendActBusinessEntity
   **/
  @Override
  public ExtendActModel queryByActKey(String actKey) {
    EntityManager em = getEntityManager();
    Query query = em.createQuery("select self from ExtendActModel self");
    List<ExtendActModel> resultList = query.getResultList();
    return resultList.get(0);
  }
}
