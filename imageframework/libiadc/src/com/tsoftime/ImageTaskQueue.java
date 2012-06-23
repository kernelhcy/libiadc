package com.tsoftime;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The image task queue
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:19
 */
public class ImageTaskQueue
{
    public ImageTaskQueue()
    {
        queue = new PriorityQueue<ImageTask>();
    }

    public ImageTaskQueue(int initialCapacity)
    {
        queue = new PriorityQueue<ImageTask>(initialCapacity);
    }

    public ImageTask dequeue()
    {
        return queue.poll();
    }

    public void enqueue(ImageTask task)
    {
        queue.add(task);
    }

    public int size()
    {
        return queue.size();
    }

    public void clear()
    {
        queue.clear();
    }

    public boolean remove(ImageTask task)
    {
        return queue.remove(task);
    }

    private Queue<ImageTask> queue;
}

