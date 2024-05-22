package com.kob.matchingsystem.service.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@Component
public class MatchingPool extends Thread{

    //储存所有玩家
    private  static List<Player>players=new ArrayList<>();
    private final ReentrantLock lock=new ReentrantLock();


    private static RestTemplate restTemplate;
    private static final String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId,Integer rating,Integer botId){
        lock.lock();
        try{
            players.add(new Player(userId,rating,botId,0));
        }finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId){
        lock.lock();
        try{
           List<Player> newPlayer=new ArrayList<>();
           for(Player players:players){
               if(!players.getUserId().equals(userId)){
                   newPlayer.add(players);
               }
           }
           players=newPlayer;
        }finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime(){
        for(Player player:players){
            player.setWaitingTime(player.getWaitingTime()+1);
        }
    }
    private Boolean checkMatched(Player a,Player b){
        int ratingDelta=Math.abs(a.getRating()-b.getRating());
        int waitingTime=Math.min(a.getWaitingTime(),b.getWaitingTime());
        return ratingDelta<=waitingTime*10;
    }


    private void matchPlayer() {   // 尝试匹配所有玩家
        System.out.println("match players: " + players.toString());
        // 标记是否被匹配
        boolean[] used = new boolean[players.size()];

        // 先枚举等待最久的玩家，恰好是players前面的玩家等待的的久
        for(int i = 0; i < players.size(); i++) {
            if(used[i]) continue;
            // i只要和比i大的匹配，就正好所有玩家匹配一次
            for(int j = i + 1; j < players.size(); j++) {
                if(used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if(checkMatched(a, b)) {
                    used[i] = used[j] = true;
                    sendResult(a, b);
                    break;
                }
            }
        }

        List<Player> newPlayers = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            if(!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }


    private void sendResult(Player a,Player b){
        System.out.println("send result: " + a + " " + b);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("a_bot_id", a.getBotId().toString());
        data.add("b_id", b.getUserId().toString());
        data.add("b_bot_id", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }


    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(1000);
                // 涉及到操作players变量，加锁；
                lock.lock();
                try {
                    increaseWaitingTime();
                    matchPlayer();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
