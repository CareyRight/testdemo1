package com.hypaas.activiti.common;

/** @ClassName Constant @Description 枚举常量 @Author GuoHongKai @Date 2020/8/6 17:15 @Version 1.0 */
public class Constant {

  /** 流程会签集合名称 */
  public static final String ACT_MUIT_LIST_NAME = "users";

  /** 流程会签变量名称 */
  public static final String ACT_MUIT_VAR_NAME = "user";

  /** 流程节点类型 */
  public enum NodeType {
    /** 开始节点 */
    START("1"),
    /** 审批节点 */
    EXAMINE("2"),

    /** 分支 */
    BRUNCH("3"),
    /** 连线 */
    LINE("4"),
    /** 结束 */
    END("5");

    private String value;

    private NodeType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** 审批节点行为 */
  public enum ActAction {
    /** 审批 */
    APPROVE("1"),
    /** 会签 */
    MULIT("2");

    private String value;

    private ActAction(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
