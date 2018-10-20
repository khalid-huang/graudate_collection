package org.sysu.nameservice.loadbalancer.rule.Ouyang.help;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 用于辅助时间槽的数据结构，特点是定长，同步
 */
public class FixedSafeDeque<T> {
    private int capacity; //容量
    private Deque<T> container;

    public FixedSafeDeque(int capacity) {
        this.capacity = capacity;
        container = new LinkedList<T>();
    }

    /** 如果返回的值非空，表示长度已经达到最大了，头已经被排出去了*/
    public T add(T value) {
        synchronized (this) {
            T temp = null; //
            while(size() >= capacity) {
                temp = container.poll();
            }
            container.offer(value);
            return temp;
        }
    }

    public T getFirst() {
        return this.container.getFirst();
    }

    public T getLast() {
        return this.container.getLast();
    }


    public Deque<T> getContainer() {
        return this.container;
    }

    public int size() {
        return container.size();
    }

    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public String toString() {
        String result =  "FixedSafeDeque{" +
                "container=";
        for(T t : container) {
            result += " " + container.toString();
        }
        result += "}";
        return result;
    }
}
