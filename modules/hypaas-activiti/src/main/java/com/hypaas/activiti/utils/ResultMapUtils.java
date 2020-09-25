package com.hypaas.activiti.utils;

import com.hypaas.activiti.exception.MyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ResultUtils @Description SQL查询结果集的映射 @Author GuoHongKai @Date 2020/8/18 9:33 @Version
 * 1.0
 */
public class ResultMapUtils {

  /*
   * @Author GuoHongKai
   * @Description SQL查询结果集list集合对Map的自动转化
   * @Date 9:35 2020/8/18
   * @Param [result, fields]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  public static List<Map<String, Object>> toResultMap(List<Object[]> result, String... fields) {
    if (result.get(0).length != fields.length) {
      throw new MyException("字段结果数量与结果集数量不一致,请检查!");
    }
    ArrayList<Map<String, Object>> maps = new ArrayList<>();
    for (int i = 0; i < result.size(); i++) {
      HashMap<String, Object> resultMap = new HashMap<>();
      for (int j = 0; j < result.get(i).length; j++) {
        resultMap.put(fields[j], result.get(i)[j]);
      }
      maps.add(resultMap);
    }
    return maps;
  }
}
