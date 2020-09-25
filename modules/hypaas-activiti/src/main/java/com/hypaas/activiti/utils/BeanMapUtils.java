package com.hypaas.activiti.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.cglib.beans.BeanMap;

/**
 * 对象属性与map集合转化工具类
 *
 * @author hkguo
 * @description
 * @version 1.0
 * @create 2020年5月25日 下午6:15:43
 */
public class BeanMapUtils {

  /**
   * 将对象属性转化为map结合
   *
   * @param <T>
   * @param bean
   * @return
   */
  public static <T> Map<String, Object> beanToMap(T bean) {
    Map<String, Object> map = new HashMap<>();
    if (bean != null) {
      BeanMap beanMap = BeanMap.create(bean);
      for (Object key : beanMap.keySet()) {
        map.put(key + "", beanMap.get(key));
      }
    }
    return map;
  }

  /**
   * 将map集合中的数据转化为指定对象的同名属性中
   *
   * @param <T>
   * @param map
   * @param clazz
   * @return
   * @throws Exception
   */
  public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) throws Exception {
    T bean = clazz.newInstance();
    BeanMap beanMap = BeanMap.create(bean);
    beanMap.putAll(map);
    return bean;
  }

  public static <T> T mapToBean2(Map<String, String> map, Class<T> clazz) throws Exception {
    T bean = clazz.newInstance();
    BeanMap beanMap = BeanMap.create(bean);
    beanMap.putAll(map);
    return bean;
  }
}
