package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class Consumer extends Thread{
    private Bot bot;
    public static RestTemplate restTemplate;

    private static final String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        Consumer.restTemplate=restTemplate;
    }
    public void startTimeout(long timeout, Bot  bot) {
        this.bot = bot;
        this.start();
        try {
            // 在程序运行结束后（也就是start线程）或程序在指定timeout时间后还未执行完毕 直接中断代码执行
            this.join(timeout);
            this.interrupt();//中断线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }




    private String addUid(String code, String uid) {    // 在code中的Bot类名后添加uid
        //先从代码中找出字符串
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        //然后把前k位前面的代码加上+uid返回然后再返回后面的
        return code.substring(0, k) + uid + code.substring(k);
    }



    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();//获取随机字符串
        String uid = uuid.toString().substring(0, 8);//只取前八位
        BotInterface botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.code" + uid,
                addUid(bot.getBotCode(), uid)//字符串形式的代码
        ).create().get();


        Integer direction = botInterface.nextMove(bot.getInput());
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
