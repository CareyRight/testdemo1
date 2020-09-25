package com.hypaas.activiti.mapper;

import com.hypaas.meta.db.MetaModel;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @InterfaceName MetaModelMapper @Description TODO @Author GuoHongKai @Date 2020/8/31
 * 13:36 @Version 1.0
 */
@Mapper
public interface MetaModelMapper {

  /*
   * @Author GuoHongKai
   * @Description 选择审批流实体
   * @Date 13:37 2020/8/31
   * @Param []
   * @return java.util.List<com.hypaas.meta.db.MetaModel>
   **/
  @Select(
      "<script>select * from meta_model a where a.full_name like 'com.hypaas.activiti.db%'</script>")
  List<MetaModel> findClassUrls();
}
