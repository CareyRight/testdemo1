package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActNodeField;
import java.util.List;
import java.util.Map;

/**
 * @InterfaceName ExtendActNodeFieldService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:36 @Version 1.0
 */
public interface ExtendActNodeFieldService {
  /*
   * @Author GuoHongKai
   * @Description 查询节点连线数据
   * @Date 13:55 2020/8/12
   * @Param [params]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeField>
   **/
  List<ExtendActNodeField> queryList(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description 根据节点集合查询
   * @Date 9:50 2020/8/21
   * @Param [nextIds]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeField>
   **/
  List<ExtendActNodeField> queryByNodes(List<String> nextIds);
}
