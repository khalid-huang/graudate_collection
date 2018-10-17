package org.sysu.nameservice.loadbalancer.rule.Ouyang.help;

public final class HelpLevel {
    private HelpLevel() {}

    /**
     *
     * @param requestNumber the number of requests processed per second
     * @param processTime the average processing time per processed request (in milliseconds)
     * @param workItems the number of work item starts and completions per second
     * @param executingThreads the number of worker threads currently executing in the engine’s container
     * @return
     */
    public static int calculateBusyness(TripleValue requestNumber, TripleValue processTime, TripleValue workItems, TripleValue executingThreads) {
        double first = (requestNumber.getFirstVal() / requestNumber.getSecondVal()) * requestNumber.getThirdVal();
        double second = (processTime.getFirstVal() / processTime.getSecondVal()) * processTime.getThirdVal();
        double third = (workItems.getFirstVal() / workItems.getSecondVal()) * workItems.getThirdVal();
        double fourth = (executingThreads.getFirstVal() / executingThreads.getSecondVal()) * executingThreads.getThirdVal();

        return new Double((first + second + third + fourth) * 100).intValue();
    }
}
