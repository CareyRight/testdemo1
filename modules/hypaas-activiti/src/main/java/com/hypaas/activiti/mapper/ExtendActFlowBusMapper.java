package com.hypaas.activiti.mapper;

import com.hypaas.activiti.db.ExtendActFlowBus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName ExtendActFlowBusMapper @Description TODO @Author GuoHongKai @Date 2020/8/27
 * 10:11 @Version 1.0
 */
@Mapper
public interface ExtendActFlowBusMapper {

  /*
   * @Author GuoHongKai
   * @Description 根据busId修改流程表信息
   * @Date 10:12 2020/8/27
   * @Param [flowBus]
   * @return void
   **/
  @Update(
      "<script>update activiti_extend_act_flow_bus\n"
          + "\t\t<set>\n"
          + "\t\t\t<if test=\"busId != null\">`bus_id` = #{busId}, </if>\n"
          + "\t\t\t<if test=\"status != null\">`status` = #{status}, </if>\n"
          + "\t\t\t<if test=\"startTime != null\">`start_time` = #{startTime}, </if>\n"
          + "\t\t\t<if test=\"instanceId != null\">`instance_id` = #{instanceId}, </if>\n"
          + "\t\t\t<if test=\"defid != null\">`defid` = #{defid}, </if>\n"
          + "\t\t\t<if test=\"startUserId != null\">`start_user_Id` = #{startUserId}, </if>\n"
          + "\t\t\t<if test=\"code != null\">`code` = #{code}, </if>\n"
          + "\t\t\t<if test=\"actKey != null\">`act_key` = #{actKey}, </if>\n"
          + "\t\t\t<if test=\"tableName != null\">`table_name` = #{tableName}</if>\n"
          + "\t\t</set>\n"
          + "\t\twhere bus_id = #{busId}</script>")
  public int updateByBusId(ExtendActFlowBus flowBus);
}
