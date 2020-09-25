package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActNodeSet;
import com.hypaas.auth.db.User;
import java.util.Set;

/**
 * @InterfaceName ExtendActNodesetService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:24 @Version 1.0
 */
public interface ExtendActNodesetService {

  /*
   * @Author GuoHongKai
   * @Description 查询扩展节点信息
   * @Date 13:54 2020/8/12
   * @Param [nodeId]
   * @return com.hypaas.activiti.db.ExtendActNodeSet
   **/
  ExtendActNodeSet queryByNodeId(String nodeId);

  /*
   * @Author GuoHongKai
   * @Description 保存扩展节点信息
   * @Date 13:56 2020/8/12
   * @Param [actModel]
   * @return com.hypaas.activiti.db.ExtendActNodeSet
   **/
  ExtendActNodeSet saveNode(ExtendActNodeSet actModel);

  /*
   * @Author GuoHongKai
   * @Description 查询该节点的类型
   * @Date 11:42 2020/8/18
   * @Param [id, id1]
   * @return com.hypaas.activiti.db.ExtendActNodeSet
   **/
  ExtendActNodeSet queryByNodeIdModelId(String id, String id1);

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:34 2020/8/18
   * @Param [nodeId]
   * @return void
   **/
  Set<User> getUsersByNodeIdModelId(String nodeId, String modelId);
}
