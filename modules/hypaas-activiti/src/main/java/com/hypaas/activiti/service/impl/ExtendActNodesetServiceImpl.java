package com.hypaas.activiti.service.impl;

import com.google.inject.Inject;
import com.hypaas.activiti.db.ExtendActNodeSet;
import com.hypaas.activiti.db.repo.ExtendActNodeSetRepository;
import com.hypaas.activiti.exception.MyException;
import com.hypaas.activiti.service.ExtendActNodesetService;
import com.hypaas.activiti.utils.StringUtils;
import com.hypaas.auth.db.User;
import com.hypaas.db.JpaSupport;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * @ClassName ExtendActNodesetService @Description TODO @Author GuoHongKai @Date 2020/8/10
 * 15:25 @Version 1.0
 */
public class ExtendActNodesetServiceImpl extends JpaSupport implements ExtendActNodesetService {

  @Inject private ExtendActNodeSetRepository nodeSetRepository;

  @Override
  public ExtendActNodeSet queryByNodeId(String nodeId) {
    EntityManager em = getEntityManager();
    Query query =
        em.createQuery(
            "select self from ExtendActNodeSet self where self.nodeId = '" + nodeId + "'");
    try {
      ExtendActNodeSet singleResult = (ExtendActNodeSet) query.getSingleResult();
      return singleResult;
    } catch (NoResultException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ExtendActNodeSet saveNode(ExtendActNodeSet actNodeset) {
    if (StringUtils.isEmpty(actNodeset.getModelId())) {
      throw new MyException("模型id不能为空!");
    }
    if (StringUtils.isEmpty(actNodeset.getNodeId())) {
      throw new MyException("节点id不能为空!");
    }
    if (StringUtils.isEmpty(actNodeset.getNodeType())) {
      throw new MyException("节点类型不能为空!");
    }
    //        //节点类型为审批节点
    //        if(actNodeset.getNodeType().equals(Constant.NodeType.EXAMINE.getValue())){
    //            //设置会签取消会签
    //            if(Constant.ActAction.MULIT.getValue().equals(actNodeset.getNodeAction())){
    //                try {
    //                    ActUtils.setMultiInstance(actNodeset.getModelId(),actNodeset.getNodeId());
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                    throw new MyException("设置会签失败");
    //                }
    //            }else {
    //                try {
    //
    // ActUtils.clearMultiInstance(actNodeset.getModelId(),actNodeset.getNodeId());
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                    throw new MyException("取消会签失败");
    //                }
    //            }
    //            if (StringUtils.isEmpty(String.valueOf(actNodeset.getId()))) {
    //                //保存节点信息
    //                actNodeset.setId(Long.valueOf(Utils.uuid()));
    //                extendActNodeSetRe.save(actNodeset);
    //            } else {
    //                //更新
    //                //保存节点信息
    //                extendActNodesetDao.update(actNodeset);
    //                //保存审批用户 先根据nodeId删除节点相关的审批用户
    //                nodeuserDao.delByNodeId(actNodeset.getNodeId());
    //            }
    //            //保存审批用户
    //            String[] userTypes = actNodeset.getUserTypes();
    //            String[] userIds = actNodeset.getUserIds();
    //            List<ExtendActNodeuserEntity> nodeUsers = new ArrayList<>();
    //            ExtendActNodeuserEntity nodeUser = null;
    //            if(userIds !=null && userIds.length>0){
    //                for (int i=0;i<userIds.length;i++){
    //                    nodeUser = new ExtendActNodeuserEntity();
    //                    nodeUser.setId(userIds[i]);
    //                    nodeUser.setUserType(userTypes[i]);
    //                    nodeUser.setNodeId(actNodeset.getNodeId());
    //                    nodeUsers.add(nodeUser);
    //                }
    //                nodeuserDao.saveBatch(nodeUsers);
    //            }
    //
    //        }
    //        //分支条件连线
    //        if(actNodeset.getNodeType().equals(NodeType.LINE.getValue())){
    //            //保存
    //            if(StringUtils.isEmpty(actNodeset.getId())){
    //                //保存节点信息
    //                actNodeset.setId(Utils.uuid());
    //                extendActNodesetDao.save(actNodeset);
    //            }else {
    //                //更新
    //                //保存节点信息
    //                extendActNodesetDao.update(actNodeset);
    //                //根据nodeId删除所有节点对应的连线条件
    //                nodefieldDao.delByNodeId(actNodeset.getNodeId());
    //            }
    //            //el条件 例如${day>3 && isagree==1}
    //            StringBuilder condition = new StringBuilder("${");
    //            if(actNodeset.getJudgList() != null && actNodeset.getJudgList().size() > 0){
    //                List<ExtendActNodefieldEntity> judgList = new ArrayList<>();
    //                int sort =0;
    //                for (ExtendActNodefieldEntity nodefield:actNodeset.getJudgList()){
    //                    if(StringUtils.isEmpty(nodefield.getFieldName()) ||
    // StringUtils.isEmpty(nodefield.getRule())){
    //                        continue;
    //                    }
    //                    Map<String, Object> map = tranceCode(nodefield);
    //                    nodefield.setId(Utils.uuid());
    //                    if(!StringUtils.isEmpty(nodefield.getElOperator())){
    //                        condition.append(" "+map.get("elOperator")+" ");
    //                    }
    //
    // condition.append(nodefield.getFieldName()).append(map.get("rule")).append(nodefield.getFieldVal());
    //                    nodefield.setNodeId(actNodeset.getNodeId());
    //                    nodefield.setSort(sort+"");
    //                    sort++;
    //                    judgList.add(nodefield);
    //                }
    //                nodefieldDao.saveBatch(judgList);
    //            }
    //            String judg = condition.append("}").toString();
    //            //添加条件
    //
    // ActUtils.setSequenceFlowCondition(actNodeset.getModelId(),actNodeset.getNodeId(),judg);
    //        }
    //        //节点类型为结束
    //        if(actNodeset.getNodeType().equals(NodeType.END.getValue())){
    //            if(StringUtils.isEmpty(actNodeset.getId())){
    //                //保存节点信息
    //                actNodeset.setId(Utils.uuid());
    //                extendActNodesetDao.save(actNodeset);
    //            }else {
    //                //更新
    //                extendActNodesetDao.update(actNodeset);
    //            }
    //        }
    return new ExtendActNodeSet();
  }

  /*
   * @Author GuoHongKai
   * @Description 查询该节点的类型
   * @Date 11:46 2020/8/18
   * @Param [nodeId, ModelId]
   * @return com.hypaas.activiti.db.ExtendActNodeSet
   **/
  @Override
  public ExtendActNodeSet queryByNodeIdModelId(String nodeId, String ModelId) {
    return nodeSetRepository.findByNodeIdModelId(nodeId, ModelId);
  }

  /*
   * @Author GuoHongKai
   * @Description 获取当前节点可选择的审批人
   * @Date 14:34 2020/8/18
   * @Param [nodeId]
   * @return void
   **/
  @Override
  public Set<User> getUsersByNodeIdModelId(String nodeId, String modelId) {
    ExtendActNodeSet nodeSet = nodeSetRepository.findByNodeIdModelId(nodeId, modelId);
    Set<User> approvers = nodeSet.getApprovers();
    return approvers;
  }
}
