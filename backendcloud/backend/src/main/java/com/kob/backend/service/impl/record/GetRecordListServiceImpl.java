package com.kob.backend.service.impl.record;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import com.kob.backend.service.record.GetRecordListService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service
public class GetRecordListServiceImpl implements GetRecordListService {
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        IPage<Record> recordIPage = new Page<>(page, 10);//每页size为10
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");//按照id逆序排序
        //将record按照ID逆序排序，并返回第page页的内容
        List<Record> records = recordMapper.selectPage(recordIPage, queryWrapper).getRecords();
        JSONObject resp = new JSONObject();//用于返回到前端
        List<JSONObject> items = new LinkedList<>();
        for(Record record : records) {
            //存储对战双方的用户名 用户头像
            User userA = userMapper.selectById(record.getAId());
            User userB = userMapper.selectById(record.getBId());
            JSONObject item = new JSONObject();
            item.put("a_photo", userA.getPhoto());
            item.put("a_username", userA.getUsername());
            item.put("b_photo", userB.getPhoto());
            item.put("b_username", userB.getUsername());
            item.put("record", record);
            String result = "平局";
            if("A".equals(record.getLoser())) result = "B胜";
            else if("B".equals(record.getLoser())) result = "A胜";
            item.put("result", result);
            items.add(item);
        }
        resp.put("records", items);
        //无条件返回所有数据条数，“null”代表满足该条件
        resp.put("records_count", recordMapper.selectCount(null));
        return resp;
    }
}
