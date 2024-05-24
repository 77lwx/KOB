package com.kob.backend.service.impl.ranklist;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.ranklist.GetRanklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRanklistServiceImpl implements GetRanklistService {

    @Autowired
    UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        IPage<User> userIPage = new Page<>(page, 3);//展示page页，最多三个
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rating");//按照rating排序
        List<User> users = userMapper.selectPage(userIPage, queryWrapper).getRecords();
        JSONObject resp = new JSONObject();
        for(User user : users)
            user.setPassword("");//由于要返回给前端，清空用户密码
        resp.put("users", users);
        resp.put("users_count", userMapper.selectCount(null));//用户总数，便于分页
        return resp;
    }
}
