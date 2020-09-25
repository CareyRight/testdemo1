package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActNodeUser;
import java.util.List;

/**
 * @InterfaceName ExtendActNodeUserService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:33 @Version 1.0
 */
public interface ExtendActNodeUserService {
  /*
   * @Author GuoHongKai
   * @Description 查询节点审批用户
   * @Date 13:55 2020/8/12
   * @Param [nodeId]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeUser>
   **/
  List<ExtendActNodeUser> getNodeUserByNodeId(String nodeId);
}
