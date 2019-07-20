package com.sanjin.ibr.core.test;

public class DemoApiImpl implements DemoApi {
    @Override
    public String test(String name) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name);
        return "hello " + name;
    }

    @Override
    public int sum(int a, int b) {
        System.out.println(a + " " + b);
        return a + b;
    }
}
