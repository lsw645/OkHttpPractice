package com.lxw.okhttp.http;

import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.connection.StreamAllocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class HttpCodec {

    //    public static final String HEAD_HOST = "Host";
//    public static final String HEAD_CONNECTION = "Connection";
    public static final String HEAD_CONTENT_TYPE = "Content-Type";
    public static final String HEAD_CONTENT_LENGTH = "Content-Length";
    public static final String HEAD_TRANSFER_ENCODING = "Transfer-Encoding";

    private OkHttpClient okHttpClient;
    private StreamAllocation streamallocation;
    private OutputStream writer;
    private InputStream reader;
    ByteBuffer byteBuffer;

    public HttpCodec(OkHttpClient okhttpClient, StreamAllocation streamAllocation,
                     InputStream reader, OutputStream writer) {
        this.okHttpClient = okhttpClient;
        this.streamallocation = streamAllocation;
        this.reader = reader;
        this.writer = writer;
        //申请足够大的内存记录读取的数据 (一行)
        byteBuffer = ByteBuffer.allocate(10 * 1024);
    }


    public void writeRequestHeaders(Request request) throws IOException {
        String requestLine = requestLine(request);
        requestHeader(request, requestLine);
    }

    private String requestLine(Request request) {
        return request.method() +
                " " +
                request.url().file() +
                " " +
                "HTTP/1.1"
                + "\r\n";
    }

    private void requestHeader(Request request, String requestLine) throws IOException {

            StringBuilder all = new StringBuilder();
            all.append(requestLine);
//            writer.write(requestLine.getBytes());

            for (Map.Entry<String, String> header : request.header().entrySet()) {
                String sb = header.getKey() +
                        ": " +
                        header.getValue() +
                        "\r\n";
                all.append(sb);
            }
            all.append("\r\n");
//            writer.write("\r\n".getBytes());
            String body = writeRequestBody(request);
            if (body != null) {
                all.append(body);
            }
            writer.write(all.toString().getBytes());
            System.out.println(all.toString());
            finishRequest();

    }


    public String writeRequestBody(Request request)  {
        if (request.body() != null) {
            return request.body().body();
        }
        return null;
    }

    public void finishRequest() throws IOException {
        writer.flush();
    }

    static final int CR = 13;
    static final int LF = 10;

    public String readLine() throws IOException {
        byte b;
        boolean isMayEOLine = false;
        byteBuffer.clear();
        byteBuffer.mark();
        while ((b = (byte) reader.read()) != -1) {
            byteBuffer.put(b);
            if (b == CR) {
                isMayEOLine = true;
            } else if (isMayEOLine) {
                if (b == LF) {
                    byte[] bytes = new byte[byteBuffer.position()];
                    byteBuffer.reset();
                    byteBuffer.get(bytes);
                    byteBuffer.clear();
                    byteBuffer.mark();
                    return new String(bytes);
                }
                isMayEOLine = false;
            }
        }
        throw new IOException("resposne read line  false ");
    }

    public HashMap<String, String> readHeader() throws IOException {
        HashMap<String, String> responseHeader = new HashMap<>();
        while (true) {
            String headLine = readLine();
            //如果是\R\N表示后面是响应体
            if (isEmptyLine(headLine)) {
                break;
            }

            int index = headLine.indexOf(":");
            if (index > 0) {
                String key = headLine.substring(0, index);
                String value = headLine.substring(index + 2, headLine.length() - 2);
                responseHeader.put(key, value);
            }
        }
        return responseHeader;
    }

    private boolean isEmptyLine(String line) {
        return line.equals("\r\n");
    }

    public byte[] readBody(Integer contentLength) throws IOException {
        byte[] bytes = new byte[contentLength];
        int readNum = 0;
        while (true) {
            readNum += reader.read(bytes, readNum, contentLength - readNum);
            if (readNum == contentLength) {
                break;
            }
        }
        return bytes;
    }

    public String readChunked() throws IOException {
        int len = -1;
        boolean isEmptyData = false;
        StringBuffer chunked = new StringBuffer();
        while (true) {
            //解析下一个chunk长度
            if (len < 0) {
                String line = readLine();
                line = line.substring(0, line.length() - 2);
                len = Integer.valueOf(line, 16);
                //chunk编码的数据最后一段为 0\r\n\r\n
                isEmptyData = len == 0;
            } else {
                //块长度不包括\r\n  所以+2将 \r\n 读走
                byte[] bytes = readBody( len + 2);
                chunked.append(new String(bytes));
                len = -1;
                if (isEmptyData) {
                    return chunked.toString();
                }
            }
        }

    }
}
