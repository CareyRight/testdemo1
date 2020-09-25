package com.hypaas.activiti.mapper;

import com.hypaas.activiti.db.ExtendActNodeField;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @InterfaceName ExtendActNodeFieldMapper @Description TODO @Author GuoHongKai @Date 2020/8/21
 * 9:53 @Version 1.0
 */
@Mapper
public interface ExtendActNodeFieldMapper {

  /*
   * @Author GuoHongKai
   * @Description 根据节点集合查询
   * @Date 9:53 2020/8/21
   * @Param [nextIds]
   * @return java.util.List<com.hypaas.activiti.db.ExtendActNodeField>
   **/
  @Select(
      "<script>select\n"
          + "id, \n"
          + "\t\tnode_id, \n"
          + "\t\tfield_name, \n"
          + "\t\tfield_type, \n"
          + "\t\trule, \n"
          + "\t\tfield_val,\n"
          + "\t\tel_operator"
          + "\t\tfrom activiti_extend_act_node_field\n"
          + "\t\twhere node_id IN\n"
          + "\t\t<foreach item=\"nodeId\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n"
          + "\t\t\t#{nodeId}\n"
          + "\t\t</foreach></script>")
  List<ExtendActNodeField> queryByNodes(List<String> nextIds);
}
