package com.tsoftime;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The image task queues
 * There three task queues:
 *  <ol>
 *      <li><b>High priority queue</b><li/>
 *      <li><b>Default priority queue</b><li/>
 *      <li><b>Low priority queue</b><li/>
 *  </ol>
 *
 * The image manager will first run the image task in the high priority queue.
 * If there is no task in the high priority queue, the image manager will run the tasks in the default priority queue.
 * If there is no task in the high priority queue and the default priority queue, the image manager will run
 * the tasks in the low priority queue.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:19
 */
class ImageTaskQueues
{
    public ImageTaskQueues()
    {
        defaultPriorityQueue = new LinkedList<ImageTask>();
        highPriorityQueue = new LinkedList<ImageTask>();
        lowPriorityQueue = new LinkedList<ImageTask>();
    }

    /**
     * Get a task.
     *
     * @return a task or null if there is no task.
     */
    public ImageTask dequeue()
    {
        if (highPriorityQueue.size() > 0) {
            return highPriorityQueue.poll();
        } else if (defaultPriorityQueue.size() > 0) {
            return defaultPriorityQueue.poll();
        } else if (lowPriorityQueue.size() > 0) {
            return lowPriorityQueue.poll();
        }
        return null;
    }

    /**
     * Find the task
     * @param url
     * @param priority
     * @return
     */
    public ImageTask findTask(String url, TaskPriority priority)
    {
        switch (priority)
        {
            case DEFAULT_PRIORITY:
                for(ImageTask t : defaultPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            case HIGH_PRIORITY:
                for(ImageTask t : highPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            case LOW_PRIORITY:
                for(ImageTask t : lowPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Enqueue a task
     * @param task
     */
    public void enqueue(ImageTask task)
    {
        switch (task.getPriority())
        {
            case HIGH_PRIORITY:
                highPriorityQueue.offer(task);
                break;
            case LOW_PRIORITY:
                lowPriorityQueue.offer(task);
                break;
            case DEFAULT_PRIORITY:
            default:
                defaultPriorityQueue.offer(task);
                break;
        }
    }

    /**
     * The total size of all the queues.
     * @return the size.
     */
    public int size()
    {
        return defaultPriorityQueue.size() + highPriorityQueue.size() + lowPriorityQueue.size();
    }

    /**
     * Remove all the tasks.
     */
    public void clear()
    {
        defaultPriorityQueue.clear();
        lowPriorityQueue.clear();
        highPriorityQueue.clear();
    }

    /**
     * Remote a task
     * @param task
     * @return true if it has been removed, or false if not found
     */
    public boolean remove(ImageTask task)
    {
        if (highPriorityQueue.contains(task)) {
            return highPriorityQueue.remove(task);
        } else if (lowPriorityQueue.contains(task)) {
            return lowPriorityQueue.remove(task);
        } else if (defaultPriorityQueue.contains(task)) {
            return defaultPriorityQueue.remove(task);
        }
        return false;
    }

    private Queue<ImageTask> defaultPriorityQueue;      // the default priority queue
    private Queue<ImageTask> highPriorityQueue;         // the high priority queue
    private Queue<ImageTask> lowPriorityQueue;          // the low priority queue
}

