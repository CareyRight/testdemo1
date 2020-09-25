package com.hypaas.activiti.service;

import com.hypaas.activiti.db.ExtendActModel;
import java.io.IOException;

/**
 * @InterfaceName ExtendActModelerService @Description TODO @Author GuoHongKai @Date 2020/8/6
 * 17:01 @Version 1.0
 */
public interface ExtendActModelerService {

  /*
   * @Author GuoHongKai
   * @Description 保存流程模型
   * @Date 16:32 2020/8/24
   * @Param [actModel]
   * @return java.lang.String
   **/
  String save(ExtendActModel actModel) throws Exception;

  /*
   * @Author GuoHongKai
   * @Description 删除流程模型
   * @Date 16:32 2020/8/24
   * @Param [id]
   * @return void
   **/
  void delete(String id);

  /*
   * @Author GuoHongKai
   * @Description 部署流程模型
   * @Date 16:32 2020/8/24
   * @Param [modelId]
   * @return void
   **/
  void deploy(String modelId) throws IOException, Exception;
}
