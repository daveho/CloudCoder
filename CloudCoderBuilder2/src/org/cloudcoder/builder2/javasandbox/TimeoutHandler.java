package org.cloudcoder.builder2.javasandbox;

/**
 * Callback handler to create a new task outcome of type T
 * when a task times out.
 * 
 * It's necessary to have a callback here because several 
 * task threads may time out, requiring the creation of multiple
 * objects for each timeout.
 * 
 */
public interface TimeoutHandler<T> {
    public T handleTimeout();
}