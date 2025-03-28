package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    private User user;//由于储存每个链接对应的用户信息
    private Session session = null;

    public Game game = null;
    private final static String addPlayerUrl="http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl="http://127.0.0.1:3001/player/remove/";
    public static UserMapper userMapper;
    public static RecordMapper recordMapper;

    public static BotMapper botMapper;

    public static RestTemplate restTemplate;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper){
        WebSocketServer.botMapper=botMapper;
    }


    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        WebSocketServer.restTemplate=restTemplate;
    }


    //链接建立时自动触发这些函数（onopen,onclose,sendmessage）
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session=session;
        System.out.println("connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if(this.user != null){
            users.put(userId,this);
        }else{
            this.session.close();
        }
        System.out.println(users);
    }

    @OnClose
    public void onClose() {
        // 关闭链接时自动调用
        System.out.println("disconnected!");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
    }

    public static void  startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId){
        User a=userMapper.selectById(aId);
        User b=userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId);
        Bot botB = botMapper.selectById(bBotId);

        Game game = new Game(
                13,
                14,
                20,
                a.getId(),
                botA,
                b.getId(),
                botB
        );
        //创建一个地图
        game.createMap();

        if(users.get(a.getId()) != null)
            users.get(a.getId()).game = game;
        if(users.get(b.getId()) != null)
            users.get(b.getId()).game = game;

        game.start();

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        //分别给user1和user2传送消息告诉他们匹配成功了

        //通过user1的连接向user1发消息
        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if(users.get(a.getId()) != null)
            users.get(a.getId()).sendMessage(respA.toJSONString());


        //通过user2的连接向user2发消息
        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if(users.get(b.getId()) != null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
    }


    private void startMatching(Integer botId) {
        System.out.println("start matching");
        //多键值对map
        MultiValueMap<String,String>data=new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        data.add("rating",this.user.getRating().toString());
        data.add("bot_id",botId.toString());
        restTemplate.postForObject(addPlayerUrl,data,String.class);

    }

    private void stopMatching() {
        System.out.println("stop matching");
        MultiValueMap<String,String>data=new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl,data,String.class);

    }

    public  void move(int direction){//接收前端的信息传给game
        System.out.println(direction);
        //判断是A玩家还是B玩家在操作
        if (game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId().equals(-1))//如果是亲自出马才传递操作
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId().equals(-1))
                game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {//当做路由一样
        // 从Client接收消息
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {//防止event为空的异常
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        }else if("move".equals(event)){
            move(data.getInteger("direction"));
        }
    }


    @OnError
    public void onError(Session session, Throwable error)  {
        error.printStackTrace();
    }


    public void sendMessage(String message){
        synchronized (this.session){
            try{
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

