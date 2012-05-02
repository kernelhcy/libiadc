package com.tsoftime.barcodelib.decode;

/**
 * User: huangcongyu2006
 * Date: 12-5-2 PM3:03
 */
public interface IDCode
{
    int decode = 1;
    int quit = 2;
    int auto_focus = 3;
    int restart_preview = 4;
    int decode_succeeded = 5;
    int decode_failed = 6;
}
