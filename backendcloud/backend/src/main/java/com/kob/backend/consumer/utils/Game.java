package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;

import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{

    final private Integer rows;
    final private Integer cols;
    final private Integer inner_walls_count;
    final private int[][] g;
    final private static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    private final Player playerA, playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing";  // playing -> finished
    private String loser = "";  // all: 平局，A: A输，B: B输

    private static final String addBotUrl = "http://127.0.0.1:3002/bot/add/";



    public Game(Integer rows,
                Integer cols,
                Integer inner_walls_count,
                Integer idA,
                Bot botA,
                Integer idB,
                Bot botB
    ) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA=-1,botIdB=-1;
        String botCodeA="",botCodeB="";

        if(botA!=null){
            botIdA=botA.getId();
            botCodeA=botA.getContent();
        }

        if(botB!=null){
            botIdB=botB.getId();
            botCodeB=botB.getContent();
        }

        playerA = new Player(idA,botIdA,botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB,botIdB,botCodeB, 1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA(){return playerA;}
    public Player getPlayerB(){return playerB;}

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }
    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    public int[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {//判断是否是连通
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i ++ ) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean draw() {  // 画地图
        for (int i = 0; i < this.rows; i ++ ) {
            for (int j = 0; j < this.cols; j ++ ) {
                g[i][j] = 0;
            }
        }

        for (int r = 0; r < this.rows; r ++ ) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c ++ ) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i ++ ) {
            for (int j = 0; j < 1000; j ++ ) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1)
                    continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
                    continue;

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i ++ ) {
            if (draw())
                break;
        }
    }

    private String getInput(Player player) {    // 将当前局面信息编码成字符串
        // 地图#my.sx#my.sy#my操作#you.sx#you.sy#you操作
        Player me, you;
        if(playerA.getId().equals(player.getId())) {//让bot知道自己是谁
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +    // 加()是为了预防操作序列为空
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }


    private void sendBotCode(Player player) {
        if(player.getBotId().equals(-1)) return;//人亲自出马，不需要执行代码
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }



    private boolean nextStep() {  // 等待两名玩家的下一步操作
        //由于前端是200毫秒渲染一次，防止用户在未渲染好久连续输入导致无效所以睡眠200毫秒
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //相当于等待bot输入了，bot代码执行一次相当于一次输入，所以每回合都要让bot执行一次代码返回结果
        sendBotCode(playerA);
        sendBotCode(playerB);

        //如果5秒内有玩家没有输入 就返回false
        for (int i = 0; i < 50; i ++ ) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {

        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        //取到A的最后一步
        //三种不合法操作: A撞墙、A撞A、A撞B
        //A撞墙
        if (g[cell.x][cell.y] == 1) return false;
        //A撞A
        for (int i = 0; i < n - 1; i ++ ) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y)
                return false;
        }
        //A撞B
        for (int i = 0; i < n - 1; i ++ ) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y)
                return false;
        }

        return true;
    }



    private void judge() {  // 判断两名玩家下一步操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        //判断两名玩家最后一步操作是否合法
        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";

            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }

    }

    private void sendAllMessage(String message) {//工具函数:向两名玩家广播信息
        if( WebSocketServer.users.get(playerA.getId()) != null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        if( WebSocketServer.users.get(playerB.getId()) != null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }


    private void sendMove() {  // 向两个Client传递移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }


    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i ++ ) {
            for (int j = 0; j < cols; j ++ ) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void updateUserRating(){
        User userA = WebSocketServer.userMapper.selectById(playerA.getId());
        User userB = WebSocketServer.userMapper.selectById(playerB.getId());
        if(loser.equals("A")){//A输了-2，B+5
            userA.setRating(userA.getRating()-2);
            userB.setRating(userB.getRating()+5);
        }else if(loser.equals("B")){//B输了-2，A+5
            userA.setRating(userA.getRating()+5);
            userB.setRating(userB.getRating()-2);
        }
        WebSocketServer.userMapper.updateById(userA);
        WebSocketServer.userMapper.updateById(userB);
    }


    private void saveToDatabase() {
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );
        updateUserRating();
        WebSocketServer.recordMapper.insert(record);
    }



    private void sendResult() {  // 向两个Client公布对战结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }


    @Override
    public void run() {
        for (int i = 0; i < 1000; i ++ ) {
            if (nextStep()) {  // 是否获取了两条蛇的下一步操作
                judge();
                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }

    }
}
