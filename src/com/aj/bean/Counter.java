package com.aj.bean;

/**
 * Created by kevin on 16-12-27.
 */

public class Counter {
    public Counter() {
        this.counter = 0;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void increase_one_step(){
        this.counter++;
    }

    private int counter;

}
