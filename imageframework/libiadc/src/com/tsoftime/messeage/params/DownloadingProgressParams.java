package com.tsoftime.messeage.params;

/**
 * The parameters of the DOWNLOADING_PROGRESS message.
 *
 * User: huangcongyu2006
 * Date: 12-6-24 PM7:46
 */
public class DownloadingProgressParams extends RawParams
{
    public int total;      // the total length of the image
    public int hasRead;    // we have gotten.
}
