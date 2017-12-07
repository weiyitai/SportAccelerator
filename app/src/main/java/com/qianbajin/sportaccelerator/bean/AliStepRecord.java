package com.qianbajin.sportaccelerator.bean;

/**
 * @author Administrator
 * @Created at 2017/11/26 0026  21:33
 * @des
 */

public class AliStepRecord {

    /**
     * biz : alipay
     * steps : 6098
     * time : 1511671884723
     */

    private String biz;
    private int steps;
    private long time;

    public AliStepRecord() {
        this.biz = "alipay";
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "AliStepRecord{" +
                "biz='" + biz + '\'' +
                ", steps=" + steps +
                ", time=" + time +
                '}';
    }
}
