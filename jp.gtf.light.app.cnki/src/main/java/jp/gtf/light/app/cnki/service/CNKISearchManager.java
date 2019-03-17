/*
 * © 2019 Gtf Software, Inc. All Rights Reserved
 * Mail fxd@hotmail.co.jp
 * More information please visit https://gtf.jp
 */
package jp.gtf.light.app.cnki.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.gtf.kernel.lang.utils.UFile;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * CNKI BREIF SEARCH MANAGER
 *
 * @author F
 */
public class CNKISearchManager {

    static final String SCDB_ENTRY_URL = "http://kns.cnki.net/kns/brief/result.aspx?dbprefix=SCDB&crossDbcodes=CJFQ,CDFD,CMFD,CPFD,IPFD,CCND,CCJD";
    static final String SCDB_SEARCH_URL = "http://kns.cnki.net/kns/request/SearchHandler.ashx";
    static final String SCDB_PAGE_FIRST = "http://kns.cnki.net/kns/brief/brief.aspx?pagename=";

    public static void search(String subjectKeyword) {
        try {
            System.out.println(URLEncoder.encode("中国学术文献网络出版总库", "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        HttpClient httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        // 1. エントリ画面を遷移、COOKIE＆SESSION情報を設定する
        try {
            sendRequest(httpClient, httpContext, RequestBuilder.get(SCDB_ENTRY_URL).build());
        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        String firstPage = null;
        try {
            // 2. リクエストを投げ、初期検索画面を設定する

            firstPage = sendRequest(httpClient, httpContext,
                    RequestBuilder.post(SCDB_SEARCH_URL)
                            .setCharset(Charset.forName("UTF-8"))
                            .addParameter("action", "")
                            .addParameter("NaviCode", "*")
                            .addParameter("ua", "1.21")
                            .addParameter("isinEn", "1")
                            .addParameter("PageName", "ASP.brief_result_aspx")
                            .addParameter("DbPrefix", "SCDB")
                            .addParameter("DbCatalog", "中国学术文献网络出版总库")
                            .addParameter("ConfigFile", "SCDB.xml")
                            .addParameter("db_opt", "CJFQ,CDFD,CMFD,CPFD,IPFD,CCND,CCJD")
                            .addParameter("txt_1_sel", "SU$%=|")
                            .addParameter("txt_1_value1", "西游")
                            .addParameter("txt_1_relation", "#CNKI_AND")
                            .addParameter("txt_1_special1", "=")
                            .addParameter("his", "0")
                            .build());
        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
            // 継続不可エラー
        }
        try {
            // 3. 最初の画面を検索
            String firstPageContent = sendRequest(httpClient, httpContext, RequestBuilder.get(
                    new StringBuilder()
                            .append(SCDB_PAGE_FIRST)
                            .append(firstPage)
                            .toString()).build());
            // 4.最初検索結果を分析する
            UFile.write("C:\\data\\result.html", firstPageContent);
        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String sendRequest(HttpClient httpClient, HttpContext httpContext, HttpUriRequest method) throws Exception {
        return IOUtils.toString(
                httpClient.execute(method, httpContext).getEntity().getContent(),
                "UTF-8");
    }
}
