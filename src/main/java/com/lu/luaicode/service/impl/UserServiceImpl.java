package com.lu.luaicode.genresult.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lu.luaicode.genresult.domain.entity.User;
import com.lu.luaicode.genresult.mapper.UserMapper;
import com.lu.luaicode.genresult.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author illusory
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
