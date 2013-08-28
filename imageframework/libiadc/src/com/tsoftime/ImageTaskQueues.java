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
        mDefaultPriorityQueue = new LinkedList<ImageTask>();
        mHighPriorityQueue = new LinkedList<ImageTask>();
        mLowPriorityQueue = new LinkedList<ImageTask>();
    }

    /**
     * Get a task.
     *
     * @return a task or null if there is no task.
     */
    public ImageTask dequeue()
    {
        if (mHighPriorityQueue.size() > 0) {
            return mHighPriorityQueue.poll();
        } else if (mDefaultPriorityQueue.size() > 0) {
            return mDefaultPriorityQueue.poll();
        } else if (mLowPriorityQueue.size() > 0) {
            return mLowPriorityQueue.poll();
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
                for(ImageTask t : mDefaultPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            case HIGH_PRIORITY:
                for(ImageTask t : mHighPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            case LOW_PRIORITY:
                for(ImageTask t : mLowPriorityQueue) {
                    if (t.getUrl().equals(url)) return t;
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Remove the task of taskId
     * @param taskId
     */
    public void removeTask(int taskId)
    {
        for(ImageTask t : mDefaultPriorityQueue) {
            if (t.getTaskId() == taskId) {
                mDefaultPriorityQueue.remove(t);
                break;
            }
        }
        for(ImageTask t : mHighPriorityQueue) {
            if (t.getTaskId() == taskId) {
                mDefaultPriorityQueue.remove(t);
                break;
            }
        }
        for(ImageTask t : mLowPriorityQueue) {
            if (t.getTaskId() == taskId) {
                mDefaultPriorityQueue.remove(t);
                break;
            }
        }
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
                mHighPriorityQueue.offer(task);
                break;
            case LOW_PRIORITY:
                mLowPriorityQueue.offer(task);
                break;
            case DEFAULT_PRIORITY:
            default:
                mDefaultPriorityQueue.offer(task);
                break;
        }
    }

    /**
     * The total size of all the queues.
     * @return the size.
     */
    public int size()
    {
        return mDefaultPriorityQueue.size() + mHighPriorityQueue.size() + mLowPriorityQueue.size();
    }

    /**
     * Remove all the tasks.
     */
    public void clear()
    {
        mDefaultPriorityQueue.clear();
        mLowPriorityQueue.clear();
        mHighPriorityQueue.clear();
    }

    /**
     * Remote a task
     * @param task
     * @return true if it has been removed, or false if not found
     */
    public boolean remove(ImageTask task)
    {
        if (mHighPriorityQueue.contains(task)) {
            return mHighPriorityQueue.remove(task);
        } else if (mLowPriorityQueue.contains(task)) {
            return mLowPriorityQueue.remove(task);
        } else if (mDefaultPriorityQueue.contains(task)) {
            return mDefaultPriorityQueue.remove(task);
        }
        return false;
    }

    private Queue<ImageTask> mDefaultPriorityQueue;      // the default priority queue
    private Queue<ImageTask> mHighPriorityQueue;         // the high priority queue
    private Queue<ImageTask> mLowPriorityQueue;          // the low priority queue
}

