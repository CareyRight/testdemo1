package com.hypaas.activiti.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/*
 * @Author GuoHongKai
 * @Description 业务流程 对应的 业务表
 * @Date 15:45 2020/8/11
 * @Param
 * @return
 **/
public class ExtendActBusinessEntity implements Serializable {
  /** 可写 */
  private List<Map<String, Object>> writes;

  /** 可设置为条件 */
  private List<Map<String, Object>> judgs;

  public List<Map<String, Object>> getWrites() {
    return writes;
  }

  public void setWrites(List<Map<String, Object>> writes) {
    this.writes = writes;
  }

  public List<Map<String, Object>> getJudgs() {
    return judgs;
  }

  public void setJudgs(List<Map<String, Object>> judgs) {
    this.judgs = judgs;
  }

  @Override
  public String toString() {
    return "ExtendActBusinessEntity{" + "writes=" + writes + ", judgs=" + judgs + '}';
  }
}
