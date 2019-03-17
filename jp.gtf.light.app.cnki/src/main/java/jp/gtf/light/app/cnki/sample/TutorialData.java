/*
 * © 2019 Gtf Software, Inc. All Rights Reserved
 * Mail fxd@hotmail.co.jp
 * More information please visit https://gtf.jp
 */
package jp.gtf.light.app.cnki.sample;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
 * TutorialData
 *
 * @author F
 */
public class TutorialData {

    public static void main(String... args) {
        try {
            kicker();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final String SCDB_ENTRY_URL = "http://kns.cnki.net/kns/brief/result.aspx?dbprefix=SCDB&crossDbcodes=CJFQ,CDFD,CMFD,CPFD,IPFD,CCND,CCJD";
    static final String SCDB_SEARCH_URL = "http://kns.cnki.net/kns/request/SearchHandler.ashx";
    static final String SCDB_PAGE_FIRST = "http://kns.cnki.net/kns/brief/brief.aspx?pagename=";

    public static void kicker() throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        HttpUriRequest method1 = RequestBuilder.get(SCDB_ENTRY_URL).build();
        HttpResponse response1 = httpClient.execute(method1, httpContext);

        System.out.println(IOUtils.toString(
                response1.getEntity().getContent(),
                "UTF-8"));

        System.out.println("***************");

        HttpUriRequest method2 = RequestBuilder.post(SCDB_SEARCH_URL)
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
                .build();

        HttpResponse response2 = httpClient.execute(method2, httpContext);
        String firstPageUrl = IOUtils.toString(
                response2.getEntity().getContent(),
                "UTF-8");
        System.out.println(firstPageUrl);

        System.out.println("***************");

        HttpUriRequest method3 = RequestBuilder.get(
                new StringBuilder()
                        .append(SCDB_PAGE_FIRST)
                        .append(firstPageUrl)
                        .toString()).build();
        HttpResponse response3 = httpClient.execute(method3, httpContext);

        String response3Data = IOUtils.toString(
                response3.getEntity().getContent(),
                "UTF-8");
        System.out.println(response3Data);
    }

}
