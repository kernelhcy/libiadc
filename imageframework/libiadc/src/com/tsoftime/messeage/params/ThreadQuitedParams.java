package com.tsoftime.messeage.params;

/**
 * The parameters of the THREAD_QUITED message.
 *
 * User: huangcongyu2006
 * Date: 12-6-24 PM7:49
 */
public class ThreadQuitedParams extends RawParams
{
    public ThreadQuitedParams(String threadName)
    {
        this.threadName = threadName;
    }
}
