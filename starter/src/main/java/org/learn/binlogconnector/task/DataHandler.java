package org.learn.binlogconnector.task;

/**
 * <pre>
 *
 */
public interface DataHandler<D> {

    /**
     * 正常数据处理
     */
    void handle(D data);
}
