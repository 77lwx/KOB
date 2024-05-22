package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread{

    private static final ReentrantLock lock=new ReentrantLock();
    private final Condition condition= lock.newCondition();//条件变量

    public final Queue<Bot> bots=new LinkedList<>();//储存bot队列任务



    public void addBot(Integer userId, String botCode, String input){
        lock.lock();
        try{
            bots.add(new Bot(userId,botCode,input));
            condition.signalAll();//加入了bot进入消息队列时唤醒线程
        }finally {
            lock.unlock();
        }

    }


    private void consume(Bot bot) {
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }


    @Override
    public void run() {
        while(true) {
            lock.lock();
            if(bots.isEmpty()) {
                try {
                    // 若执行了await会自动释放锁
                    condition.await();//堵塞线程直到被线程唤醒或者终止
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove();
                lock.unlock();
                // 耗时操作，因此要在释放锁之后执行
                consume(bot);
            }
        }
    }
}


