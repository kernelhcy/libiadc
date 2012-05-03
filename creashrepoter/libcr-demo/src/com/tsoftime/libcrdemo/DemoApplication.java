package com.tsoftime.libcrdemo;

import android.app.Application;
import com.tsoftime.libcr.TSHttpPostSender;
import org.acra.*;
import org.acra.annotation.*;

/**
 * User: huangcongyu2006
 * Date: 12-4-28 PM4:49
 */
@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.SILENT)
public class DemoApplication extends Application
{
    @Override
    public void onCreate()
    {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        ErrorReporter.getInstance().setReportSender(new TSHttpPostSender(""));
        super.onCreate();
    }
}
