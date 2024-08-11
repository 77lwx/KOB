package com.kob.backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kob.backend.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper//自动实现增删查改函数，继承了BaseMapper<Bot>   同时不需要写mapper.xml文件
public interface UserMapper extends BaseMapper<User> {
}
