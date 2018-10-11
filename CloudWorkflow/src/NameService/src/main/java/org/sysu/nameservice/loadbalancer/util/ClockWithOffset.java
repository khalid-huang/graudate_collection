package org.sysu.nameservice.loadbalancer.util;

/**
 * A {@link Clock} that provides a way to modify the time returned by
 * {@link System#currentTimeMillis()}.
 * <p/>
 * This can be used during application shutdown to force the clock forward and get the
 * latest values which normally
 * would not be returned until the next step boundary is crossed.
 */
public enum ClockWithOffset implements Clock {
    /**
     * Singleton
     */
    INSTANCE;

    private volatile long offset = 0L;

    /**
     * set the offset for the clock;
     * @param offset Number of millisecons to add to the current time;
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public long now() {
        return offset + System.currentTimeMillis();
    }

}
