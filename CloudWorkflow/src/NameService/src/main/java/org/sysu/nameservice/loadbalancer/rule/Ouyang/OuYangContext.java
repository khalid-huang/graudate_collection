package org.sysu.nameservice.loadbalancer.rule.Ouyang;

/**
 * 用于设置一些配置信息和常量
 */
public class OuYangContext {
    /** level one */
    public static final int levelOneRequestNumberLimit = 100;
    public static final double levelOneRequestNumberWeight = 0.25;

    public static final int levelOneAverageProcessTimeLimit = 100;
    public static final double levelOneAverageProcessTimeWeight = 0.25;

    public static final int levelOneWorkItemLimit = 200;
    public static final double levelOneWorkItemWeight = 0.25;

    public static final int levelOneExecutingThreadsLimit = 100;
    public static final double levelOneExecutingThreadsWeight = 0.25;

    /** 就是一个时间槽*/
    public static final int levelOneMultiplePastTimeSlotSize = 1;
    public static final long levelOneSingleTimeSlotInterval = 3 * 1000;

    /** level two */
    public static final int levelTwoRequestNumberLimit = 100;
    public static final double levelTwoRequestNumberWeight = 0.25;

    public static final int levelTwoAverageProcessTimeLimit = 100;
    public static final double levelTwoAverageProcessTimeWeight = 0.25;

    public static final int levelTwoWorkItemLimit = 200;
    public static final double levelTwoWorkItemWeight = 0.25;

    public static final int levelTwoExecutingThreadsLimit = 100;
    public static final double levelTwoExecutingThreadsWeight = 0.25;

    public static final int levelTwoMultiplePastTimeSlotSize = 40; //表示时间槽中缓存了40 * 3 = 120s的数据；
    public static final long levelTwoSingleTimeSlotInterval = 3 * 1000;
    /** 表示获取从当前到过去5分钟内的数据的busyness indicator */
    public static final long levelTwoPastTime = 5 * 60000;
    public static final double levelTwoAlpha = 0.7;

    /** level three*/
    public static final long levelThreeRequestNumberLimit = 100;
    public static final double levelThreeRequestNumberWeight = 0.25;

    public static final long levelThreeAverageProcessTimeLimit = 100;
    public static final double levelThreeAverageProcessTimeWeight = 0.25;

    public static final long levelThreeWorkItemLimit = 200;
    public static final double levelThreeWorkItemWeight = 0.25;

    public static final long levelThreeExecutingThreadsLimit = 100;
    public static final double levelThreeExecutingThreadsWeight = 0.25;

    public static final int levelThreeMultiplePastTimeSlotSize = 1;
    public static final long levelThreeSingleTimeSlotInterval = 30000;


}
