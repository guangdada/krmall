package com.platform.utils.sms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MsgUtils {
	public static String request(String mobile, String content) {

		String httpUrl = "https://sh2.ipyy.com/smsJson.aspx?action=send&userid=2450&";
		String httpArg = "";
		try {
			httpArg = "content=" + URLEncoder.encode(content, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		httpArg = "mobile=" + mobile.trim() + "&account=jkwl481&password=jkwl48121&" + httpArg;
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		httpUrl = httpUrl + "" + httpArg;

		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			// 填入apikey到HTTP header

			connection.setRequestProperty("account", "jkwl481");
			connection.setRequestProperty("password", "jkwl48121");
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String mobile = "18508443775";
		String qjm = "1234";
		System.out.println(request(mobile, "【小区驿站】您的验证码是：0001"));
	}
}
