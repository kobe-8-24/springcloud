package com.htsc.leetcode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompleteFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("当前线程名称：" + Thread.currentThread().getName());
                    int i = 5 / 0;
                    return "捡田螺的小男孩";
                }
        ).whenComplete((a, throwable) -> {
//            try {
////                int i = 5 / 0;
//            } catch (Exception e) {
//                throw new RuntimeException("whenComplete exception");
//            }
            System.out.println("当前线程名称：" + Thread.currentThread().getName());
            System.out.println("上个任务执行完啦，还把" + a + "传过来");
            if ("捡田螺的小男孩".equals(a)) {
                System.out.println("666");
            }
            System.out.println("233333");
        }).exceptionally((throwable) -> {
            System.out.println(throwable.getMessage());
            return "异常了！！！";
        });

        System.out.println(stringCompletableFuture.get());
    }
}
