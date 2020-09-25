package com.hypaas.activiti.utils;

import com.google.inject.Inject;
import com.hypaas.auth.db.User;
import com.hypaas.auth.db.repo.UserRepository;

/** @ClassName UserUtils @Description TODO @Author GuoHongKai @Date 2020/8/22 11:13 @Version 1.0 */
public class UserUtils {

  @Inject private static UserRepository userRepository;

  /*
   * @Author GuoHongKai
   * @Description 根据用户id获取用户
   * @Date 11:14 2020/8/22
   * @Param [userId]
   * @return com.hypaas.auth.db.User
   **/
  public static User getUserByUserId(long userId) {
    User user = userRepository.find(userId);
    return user;
  }
}
