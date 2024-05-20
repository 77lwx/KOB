package com.kob.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kob.backend.pojo.Bot;
import org.apache.ibatis.annotations.Mapper;

@Mapper//自动实现增删查改函数，继承了BaseMapper<Bot>
public interface BotMapper extends BaseMapper<Bot> {
}
