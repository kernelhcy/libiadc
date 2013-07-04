package com.tsoftime;

/**
 * User: huangcongyu2006
 * Date: 12-8-6 PM3:06
 */
public enum TaskPriority
{
    LOW_PRIORITY            // low priority
        {
            @Override
            public String toString()
            {
                return "LowPriority";
            }
        },
    DEFAULT_PRIORITY        // default priority
        {
            @Override
            public String toString()
            {
                return "DefaultPriority";
            }
        },
    HIGH_PRIORITY           // high priority
        {
            @Override
            public String toString()
            {
                return "HighPriority";
            }
        };

    public abstract String toString();
}