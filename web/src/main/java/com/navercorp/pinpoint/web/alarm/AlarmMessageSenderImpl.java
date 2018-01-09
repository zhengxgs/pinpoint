package com.navercorp.pinpoint.web.alarm;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.apache.hadoop.hbase.shaded.org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by zhengxgs on 2017/9/9.
 */
@Service
public class AlarmMessageSenderImpl implements AlarmMessageSender{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {

        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        List<String> sms = checker.getSmsMessage();
        for (String id : receivers) {
            for (String message : sms) {
                logger.error("send SMS : {}", message);
                // TODO Implement logic for sending SMS
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msgtype", "text");
                    jsonObject.put("agentid", "1");
                    jsonObject.put("safe", "0");
                    // jsonObject.put(attr[0], attr[1]);
                    // jsonObject.put("touser", "16");
                    jsonObject.put("touser", id);
                    JSONObject j = new JSONObject();
                    j.put("content", message);
                    jsonObject.put("text", j);
                    sendWeixinHttp(getToken(), jsonObject.toJSONString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendWeixinHttp(String token, String params) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;
        // String params = "{\"toparty\":\"6\",\"msgtype\":\"text\",\"agentid\":\"1\",\"text\":{\"content\": \"" + content + "\"},\"safe\",\"0\"}";

        System.out.println("url:" + url + " params:" + params);
        String response = httpPostSend(url, params);

        JsonParser jsonParser = new JsonParser();
        JsonElement parse = jsonParser.parse(response);
        JsonElement access_token = parse.getAsJsonObject().get("errmsg");
        if (!access_token.getAsString().equals("ok")) {
            System.out.println("微信通知失败");
        }
    }

    /**
     * 获取微信token
     * @return
     */
    private String getToken() {
        String corpId = "wx6810a1001ac73cec";
        String corpsecret = "OcPsQwhN8kt9ko3CA9tJeK6ErW3rWuo4vG857hg44qYgiRBp5_1JCjvl-N_lzOQT";
        String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

        String url = String.format(tokenUrl, corpId, corpsecret);
        String response = httpGetSend(url);
        JsonParser jsonParser = new JsonParser();
        JsonElement parse = jsonParser.parse(response);
        JsonElement access_token = parse.getAsJsonObject().get("access_token");
        return access_token.getAsString();
    }

    private String httpGetSend(String getUrl) {
        URL url = null;
        InputStream in = null;
        URLConnection conn = null;
        try {
            url = new URL(getUrl);
            conn = url.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);

            in = conn.getInputStream();
            StringBuilder sb = new StringBuilder();

//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String line = null;
//            try {
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line + "\n");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            // sb.append(Files.forIO().readFrom(in, "utf-8")).append("");
            sb.append(IOUtils.toString(in, "utf-8")).append("");
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private String httpPostSend(String urlPrefix, String content) {
        URL url = null;
        InputStream in = null;
        OutputStreamWriter writer = null;
        URLConnection conn = null;
        try {
            url = new URL(urlPrefix);
            conn = url.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
            writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(content);
            writer.flush();

            in = conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            sb.append(IOUtils.toString(in, "utf-8")).append("");
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }


    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {

    }
}
