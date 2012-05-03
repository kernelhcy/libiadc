package com.tsoftime.libcr;

import android.util.Log;
import org.acra.ACRA;
import org.acra.CrashReportData;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Send the crash report to the server as JSON format
 * User: huangcongyu2006
 * Date: 12-5-3 PM4:22
 */
public class TSHttpPostSender implements ReportSender
{
    /**
     * Create a TSHttpPostSender
     *
     * @param key the key get from the crash report system administrator
     */
    public TSHttpPostSender(String key)
    {
        this(key, "192.168.1.28", "/acra/upload", 3000);
    }

    /**
     * Create a TSHttpPostSender
     *
     * @param key the key get from the crash report system administrator
     * @param host the host of the server
     * @param path the path
     * @param port the port
     */
    public TSHttpPostSender(String key, String host, String path, int port)
    {
        ErrorReporter.getInstance().putCustomData("TS_APP_KEY", key);
        this.host = host;
        this.path = path;
        this.port = port;
    }

    @Override
    public void send(CrashReportData crashReportData) throws ReportSenderException
    {
        final Map<String, String> finalReport = remap(crashReportData);
        final JSONObject json = toJSON(finalReport);
        try {
            final String jsonStr = json.toString(4);

            JSONObject result = sendPostRequest(jsonStr);
            int success = result.getInt("success");
            if (success != 1) {
                Log.e(TAG, "Send crash report data error");
                throw new ReportSenderException("Send crash report data error.", new Exception());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ReportSenderException("Create JSON date error.", e);
        }
    }


    private Map<String, String> remap(Map<ReportField, String> report)
    {
        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRA.DEFAULT_REPORT_FIELDS;
        }

        final Map<String, String> finalReport = new HashMap<String, String>(report.size());
        for (ReportField field : fields) {
            finalReport.put(field.toString(), report.get(field));
        }
        return finalReport;
    }

    /**
     * create the JSON object from the report data
     *
     * @param report
     * @return
     */
    private JSONObject toJSON(Map<String, String> report)
    {
        JSONObject json = new JSONObject();
        Set<String> keySet = report.keySet();
        Iterator<String> itor = keySet.iterator();
        while (itor.hasNext()) {
            final String key = itor.next();
            final String value = report.get(key);
            try {
                json.put(key, value);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return json;
    }

    /**
     * Post data to server
     *
     * @param content
     * @return the response's content
     */
    private JSONObject sendPostRequest(String content)
    {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;

        StringEntity entity = null;
        try {
            entity = new StringEntity(content);
        } catch (UnsupportedEncodingException e1) {
            Log.e(TAG, e1.getMessage());
            return null;
        }
        entity.setContentType("application/json");

        try {
            // 构建uri
            URI uri = URIUtils.createURI("http", host, port, path, null, null);
            Log.d(TAG, uri.toASCIIString());
            // 创建post
            HttpPost post = new HttpPost(uri);
            post.setEntity(entity);
            // 发送请求
            response = client.execute(post);
            // 验证返回的结果
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(TAG, "Status code: " + response.getStatusLine().getStatusCode());
                return new JSONObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.getConnectionManager().shutdown();
            return null;
        }
        return getContentAsJSON(response);
    }

    /**
     * Get the content from the http response
     * @param response
     * @return
     */
    private JSONObject getContentAsJSON(HttpResponse response)
    {
        byte[] buf = new byte[128];
        ByteArrayBuffer bab = new ByteArrayBuffer(128);
        int rendcnt;
        try {
            InputStream is = response.getEntity().getContent();
            while (true) {
                rendcnt = is.read(buf);
                if (rendcnt == -1) {
                    break;
                }
                bab.append(buf, 0, rendcnt);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String content = new String(bab.toByteArray());
        JSONObject json = null;
        try {
            json = new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
        return json;
    }

    private static final String TAG = TSHttpPostSender.class.getSimpleName();
    private String host;
    private String path;
    private int port;
}
