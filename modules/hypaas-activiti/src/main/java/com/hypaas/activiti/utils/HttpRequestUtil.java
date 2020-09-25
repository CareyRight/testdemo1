package com.hypaas.activiti.utils;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

public class HttpRequestUtil {
  static boolean proxySet = false;
  static String proxyHost = "127.0.0.1";
  static int proxyPort = 8087;

  public static String urlEncode(String source, String encode) {
    String result = source;
    try {
      result = URLEncoder.encode(source, encode);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "0";
    }
    return result;
  }

  public static String urlEncodeGBK(String source) {
    String result = source;
    try {
      result = URLEncoder.encode(source, "GBK");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "0";
    }
    return result;
  }

  public static String httpRequest(String req_url) {
    StringBuffer buffer = new StringBuffer();
    try {
      URL url = new URL(req_url);
      HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

      httpUrlConn.setDoOutput(false);
      httpUrlConn.setDoInput(true);
      httpUrlConn.setUseCaches(false);

      httpUrlConn.setRequestMethod("GET");
      httpUrlConn.connect();

      // 将返回的输入流转换成字符串
      InputStream inputStream = httpUrlConn.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      String str = null;
      while ((str = bufferedReader.readLine()) != null) {
        buffer.append(str);
      }
      bufferedReader.close();
      inputStreamReader.close();
      // 释放资源
      inputStream.close();
      inputStream = null;
      httpUrlConn.disconnect();

    } catch (Exception e) {
      System.out.println(e.getStackTrace());
    }
    return buffer.toString();
  }

  public static InputStream httpRequestIO(String requestUrl) {
    InputStream inputStream = null;
    try {
      URL url = new URL(requestUrl);
      HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
      httpUrlConn.setDoInput(true);
      httpUrlConn.setRequestMethod("GET");
      httpUrlConn.connect();
      // 获得返回的输入流
      inputStream = httpUrlConn.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return inputStream;
  }

  public static String sendGet(String url, String param) throws Exception {
    StringBuffer result = new StringBuffer();
    BufferedReader in = null;
    try {
      String urlNameString = url + "?" + param;
      URL realUrl = new URL(urlNameString);
      // 打开和URL之间的连接
      URLConnection connection = realUrl.openConnection();
      // 设置通用的请求属性
      connection.setRequestProperty("accept", "*/*");
      connection.setRequestProperty("Charset", "UTF-8");
      connection.setRequestProperty("connection", "Keep-Alive");
      connection.setRequestProperty(
          "user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
      /* optional request header */
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      // 建立实际的连接
      connection.connect();
      // 获取所有响应头字段
      Map<String, List<String>> map = connection.getHeaderFields();
      // 定义 BufferedReader输入流来读取URL的响应
      in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        result.append(line);
      }
    } catch (Exception e) {
      System.out.println("发送GET请求出现异常！" + e);
      e.printStackTrace();
      throw e;
    }
    // 使用finally块来关闭输入流
    finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
    return result.toString();
  }

  public static String sendPost(String url, String param, boolean isproxy) throws Exception {
    OutputStreamWriter out = null;
    // OutputStream out = null;
    BufferedReader in = null;
    StringBuffer result = new StringBuffer();
    try {
      URL realUrl = new URL(url);
      HttpURLConnection conn = null;
      if (isproxy) { // 使用代理模式
        @SuppressWarnings("static-access")
        Proxy proxy =
            new Proxy(Proxy.Type.DIRECT.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        conn = (HttpURLConnection) realUrl.openConnection(proxy);
      } else {
        conn = (HttpURLConnection) realUrl.openConnection();
      }
      // 发送POST请求必须设置如下两行
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST"); // POST方法
      // 设置通用的请求属性
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Charset", "UTF-8");
      conn.setRequestProperty("connection", "Keep-Alive");
      conn.setRequestProperty(
          "user-agent",
          "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

      conn.connect();

      // 获取URLConnection对象对应的输出流
      out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      // 发送请求参数
      out.write(param);
      // out = conn.getOutputStream();
      // out.write(param.getBytes());
      // flush输出流的缓冲
      out.flush();
      // 定义BufferedReader输入流来读取URL的响应
      in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;

      while ((line = in.readLine()) != null) {
        result.append(line).append("\n");
      }
    } catch (Exception e) {
      System.out.println("发送 POST 请求出现异常！" + e);
      e.printStackTrace();
      throw e;
    }
    // 使用finally块来关闭输出流、输入流
    finally {
      try {
        if (out != null) {
          out.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return result.toString();
  }
  // 打印cookie信息
  public static void printCookie(CookieStore cookieStore) {
    List<HttpCookie> listCookie = cookieStore.getCookies();
    listCookie.forEach(
        httpCookie -> {
          System.out.println("--------------------------------------");
          System.out.println("class      : " + httpCookie.getClass());
          System.out.println("comment    : " + httpCookie.getComment());
          System.out.println("commentURL : " + httpCookie.getCommentURL());
          System.out.println("discard    : " + httpCookie.getDiscard());
          System.out.println("domain     : " + httpCookie.getDomain());
          System.out.println("maxAge     : " + httpCookie.getMaxAge());
          System.out.println("name       : " + httpCookie.getName());
          System.out.println("path       : " + httpCookie.getPath());
          System.out.println("portlist   : " + httpCookie.getPortlist());
          System.out.println("secure     : " + httpCookie.getSecure());
          System.out.println("value      : " + httpCookie.getValue());
          System.out.println("version    : " + httpCookie.getVersion());
          System.out.println("httpCookie : " + httpCookie);
        });
  }
}
