package com.hypaas.activiti.mapper;

import com.hypaas.activiti.db.ProcessTaskDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName ActExtendMapper @Description TODO @Author GuoHongKai @Date 2020/8/19 9:32 @Version 1.0
 */
@Mapper
public interface ActExtendMapper {

  /*
   * @Author GuoHongKai
   * @Description <!--流程根据业务id查询业务信息-->
   * @Date 16:05 2020/8/19
   * @Param [params]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @Select("<script>select * from ${tableName} where ${pkName} =#{id}</script>")
  Map<String, Object> queryBusiByBusId(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description  更新当前操作流程的业务表信息
   * @Date 16:05 2020/8/19
   * @Param [busParams]
   * @return void
   **/
  @Update(
      "<script>update ${tableName}\n"
          + "\t\t<set>\n"
          + "\t\t\t<if test=\"instanceId != null\">instance_id= #{instanceId}, </if>\n"
          + "\t\t\t<if test=\"defid != null\">defid= #{defid}, </if>\n"
          + "\t\t\t<if test=\"startUserId!= null\">start_user= #{startUserId}, </if>\n"
          + "\t\t\t<if test=\"code != null\">code= #{code}, </if>\n"
          + "\t\t\t<if test=\"startTime != null\">start_time= #{startTime}, </if>\n"
          + "\t\t\t<if test=\"status != null\">status = #{status}, </if>\n"
          + "\t\t\t<if test=\"actResult != null\">act_result = #{actResult}, </if>\n"
          + "\t\t</set>\n"
          + "\t\twhere  ${pkName} =#{id}</script>")
  void updateBusInfo(Map<String, Object> busParams);

  /*
   * @Author GuoHongKai
   * @Description 查找我的待办列表
   * @Date 17:41 2020/8/19
   * @Param [params]
   * @return void
   **/

  @Select(
      "<script>select  a.name_ AS taskName,a.CREATE_TIME_ AS createTime, a.id_ AS taskId,  a.proc_inst_id_ AS instanceId,fb.defid AS defId,\n"
          + "\t\t\tfb.code,fb.act_key AS actKey,fb.bus_id AS busId,\n"
          + "\t\t\t(SELECT name FROM extend_act_business ab WHERE fb.act_key=ab.act_key ) AS busName,\n"
          + "\t\t\t(SELECT user_name FROM sp_sys_user u WHERE u.id =fb.start_user_Id)AS startUserName,\n"
          + "\t\t\t(SELECT user_name FROM sp_sys_user u WHERE u.id =a.ASSIGNEE_ ) AS dealName,\n"
          + "\t\t\t(SELECT node_type FROM extend_act_nodeset ns WHERE ns.defid=fb.defid) AS nodeType\n"
          + "\t\tFROM extend_act_flowbus fb,act_ru_task a\n"
          + "\t\tWHERE a.proc_inst_id_= fb.instance_id\n"
          + "\t\t<if test=\"dealId != null and dealId != '' \">\n"
          + "\t\t\tAND ASSIGNEE_ = #{dealId}\n"
          + "\t\t</if>\n"
          + "\t\t<if test=\"code != null and code != '' \">\n"
          + "\t\t\tAND fb.code LIKE concat('%',#{code},'%')\n"
          + "\t\t</if>\n"
          + "\t\t<if test=\"busId != null and busId != '' \">\n"
          + "\t\t\tAND fb.bus_id = #{busId}\n"
          + "\t\t</if>\n"
          + "\t\tORDER BY a.CREATE_TIME_ DESC</script>")
  void findMyUpcomingPage(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description 更新当前操作流程的业务表在审批过程中可更改的信息
   * @Date 15:24 2020/8/21
   * @Param [params]
   * @return int
   **/
  @Update(
      "<script>update ${tableName}\n"
          + "\t\t<set>\n"
          + "\t\t\t<foreach collection=\"fields\" index=\"key\" item=\"filed\" separator=\",\">\n"
          + "\t\t\t\t${filed.fieldName} = #{filed.fieldValue}\n"
          + "\t\t\t</foreach>\n"
          + "\t\t\t<if test=\"instanceId != null\">instance_id= #{instanceId}, </if>\n"
          + "\t\t</set>\n"
          + "\t\twhere  ${pkName} =#{id}</script>")
  int updateChangeBusInfo(Map<String, Object> params);

  /*
   * @Author GuoHongKai
   * @Description 查找我的待办列表
   * @Date 15:48 2020/8/22
   * @Param [params]
   * @return void
   **/
  @Select(
      "<script>select  a.create_time,a.deal_time,a.bus_id AS busId,a.def_id AS defId,a.instance_id AS instanceId,a.task_id AS taskId,\n"
          + "\t\ta.task_name AS taskName,a.app_opinion AS remark,fb.act_key,\n"
          + "\t\t(SELECT process_type FROM activiti_extend_act_business ab WHERE fb.act_key=ab.act_key ) AS busName,\n"
          + "\t\t(SELECT name FROM auth_user u WHERE u.id =fb.start_user_Id)AS startUserName,\n"
          + "\t\t(SELECT name FROM auth_user u WHERE u.id =a.deal_id ) AS dealName,\n"
          + "\t\t(SELECT name FROM auth_user u WHERE u.id =a.advance_id ) AS advanceName,\n"
          + "\t\t(SELECT node_type FROM activiti_extend_act_node_set ns WHERE ns.defid=fb.defid) AS nodeType\n"
          + "\t\tFROM activiti_extend_act_flow_bus fb,activiti_extend_act_task_log a\n"
          + "\t\tWHERE a.instance_id= fb.instance_id\n"
          + "\t\t<if test=\"dealId != null and dealId != '' \">\n"
          + "\t\t\tAND a.deal_id = #{dealId}\n"
          + "\t\t</if>\n"
          + "\t\t<if test=\"code != null and code != '' \">\n"
          + "\t\t\tAND fb.code LIKE concat('%',#{code},'%')\n"
          + "\t\t</if>\n"
          + "\t\t<if test=\"busId != null and busId != '' \">\n"
          + "\t\t\tAND fb.bus_id = #{busId}\n"
          + "\t\t</if>\n"
          + "\t\tORDER BY a.deal_time DESC</script>")
  List<ProcessTaskDto> findMyDoneList(Map<String, Object> params);
}
