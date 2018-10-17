package org.sysu.nameservice.loadbalancer.rule.Ouyang.help;

public class TripleValue {
    private long firstVal; //count
    private long secondVal;// limit
    private double thirdVal;//weight

    public TripleValue(long firstVal, long secondVal, double thirdVal) {
        this.firstVal = firstVal;
        this.secondVal = secondVal;
        this.thirdVal = thirdVal;
    }

    public long getFirstVal() {
        return firstVal;
    }

    public void setFirstVal(long firstVal) {
        this.firstVal = firstVal;
    }

    public double getSecondVal() {
        return secondVal;
    }

    public void setSecondVal(long secondVal) {
        this.secondVal = secondVal;
    }

    public double getThirdVal() {
        return thirdVal;
    }

    public void setThirdVal(double thirdVal) {
        this.thirdVal = thirdVal;
    }
}
