package com.tsoftime;

/**
 * The image quality
 *
 * @author huangcongyu2006
 * Date: 12-8-15 PM9:46
 */
public enum ImageQuality
{
    QUALITY_HIGH           // normal size image
        {
            @Override
            public String toString()
            {
                return "high_quality";
            }
        },
    QUALITY_MEDIUM         // normal size / 2
        {
            @Override
            public String toString()
            {
                return "medium_quality";
            }
        },
    QUALITY_LOW            // normal size / 4
        {
            @Override
            public String toString()
            {
                return "low_quality";
            }
        };

    public abstract String toString();
 }
