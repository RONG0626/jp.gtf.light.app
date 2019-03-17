/*
 * © 2019 Gtf Software, Inc. All Rights Reserved
 * Mail fxd@hotmail.co.jp
 * More information please visit https://gtf.jp
 */
package jp.gtf.light.app.cnki.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.gtf.kernel.lang.ms.toolkit.MSExcel;
import jp.gtf.light.app.cnki.object.DocReference;
import lombok.Getter;
import lombok.Setter;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * CNKI BREIF SEARCH MANAGER
 *
 * @author F
 */
public class CNKISearchManager {

    public enum DBCATELOG {
        SCDB,
        CJFQ,
        CDMD
    }

    static final String ENTRY_URL = "http://kns.cnki.net/kns/brief/result.aspx?dbprefix=SCDB&crossDbcodes=CJFQ,CDFD,CMFD,CPFD,IPFD,CCND,CCJD";
    static final String SEARCH_URL = "http://kns.cnki.net/kns/request/SearchHandler.ashx";
    static final String SCDB_PAGE_FIRST = "http://kns.cnki.net/kns/brief/brief.aspx?RecordsPerPage=50&pagename=";

    static final int RECORD_PRE_PAGE = 50;

    @Getter
    @Setter
    private static class RecordMeta {

        private int total = 0;
        private int pages = 0;
    }

    /**
     * 指定されたキーワードで、文献を検索する
     *
     * @param keyType 検索キータイプ
     * @param keyValue 検索キー値
     * @return 結果
     */
    public static List<DocReference> search(String keyType, String keyValue) {
        List<DocReference> totalRecords = new ArrayList<>();

        HttpClient httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        // 1. エントリ画面を遷移、COOKIE＆SESSION情報を設定する
        try {
            sendRequest(httpClient, httpContext, RequestBuilder.get(ENTRY_URL).build());
        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
            return totalRecords;
        }
        try {
            _search(httpClient, httpContext, keyType, keyValue, totalRecords);
        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return totalRecords;
    }

    private static void _search(
            HttpClient httpClient,
            HttpContext httpContext,
            String keyType,
            String keyValue,
            List<DocReference> totalRecords) throws Exception {
        String firstPage = null;
        try {
            // 2.リクエストを投げ、初期検索画面を設定する
            firstPage = sendRequest(httpClient, httpContext,
                    RequestBuilder.post(SEARCH_URL)
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
                            .addParameter("txt_1_sel", keyType)
                            .addParameter("txt_1_value1", keyValue)
                            .addParameter("txt_1_relation", "#CNKI_AND")
                            .addParameter("txt_1_special1", "=")
                            .addParameter("his", "0")
                            .build());

        } catch (Exception ex) {
            Logger.getLogger(CNKISearchManager.class.getName()).log(Level.SEVERE, null, ex);
            // 継続不可エラー
        }
        // 3.最初の画面を検索
        String firstPageContent = sendRequest(httpClient, httpContext, RequestBuilder.get(
                new StringBuilder()
                        .append(SCDB_PAGE_FIRST)
                        .append(firstPage)
                        .toString()).build());
        // 4.最初検索結果を分析する
        RecordMeta pageMeta = parseFirstPage(firstPageContent);
        for (int i = 0; i < pageMeta.pages; ++i) {
            // 5.各ページの情報を取得する
            List<DocReference> records = parseEveryPage(sendRequest(httpClient, httpContext, RequestBuilder.get(
                    new StringBuilder()
                            .append(SCDB_PAGE_FIRST)
                            .append(firstPage)
                            .append("&")
                            .append("curpage=")
                            .append(i + 1)
                            .toString()).build()));
            if (!records.isEmpty()) {
                totalRecords.addAll(records);
            }
        }
    }

    private static String sendRequest(HttpClient httpClient, HttpContext httpContext, HttpUriRequest method) throws Exception {
        return IOUtils.toString(
                httpClient.execute(method, httpContext).getEntity().getContent(),
                "UTF-8");
    }

    private static RecordMeta parseFirstPage(String htmlContent) {
        RecordMeta pageMeta = new RecordMeta();
        Document doc = Jsoup.parse(htmlContent);
        Element resultCount = doc.getElementById("resultcount");
        if (resultCount != null) {
            int count = Integer.parseInt(resultCount.attr("value"));
            pageMeta.setPages((int) Math.ceil((float) count / RECORD_PRE_PAGE));
            pageMeta.setTotal(count);
        }
        return pageMeta;
    }

    private static List<DocReference> parseEveryPage(String htmlContent) {
        List<DocReference> docReferences = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);
        Elements gridTableContentElement = doc.getElementsByClass("GridTableContent");
        if (gridTableContentElement == null) {
            return docReferences;
        }
        Elements rowElements = gridTableContentElement.select("tbody tr");
        Iterator<Element> itRow = rowElements.iterator();
        // skip first row
        itRow.next();
        while (itRow.hasNext()) {
            Element rowElement = itRow.next();
            Elements tdElements = rowElement.select("td");
            Iterator<Element> tdIt = tdElements.iterator();
            // skip id
            tdIt.next();
            DocReference ref = new DocReference();
            ref.setSubject(tdIt.next().text());
            ref.setAuthor(tdIt.next().text());
            ref.setFrom(tdIt.next().text());
            ref.setPublishedDate(tdIt.next().text());
            docReferences.add(ref);
        }
        return docReferences;
    }

    /**
     * 検索結果をEXCELに保存する
     *
     * @param datas 検索データ
     * @param filePath ファイルパス
     */
    public static void saveToExcel(List<DocReference> datas, String filePath) {
        MSExcel excel = new MSExcel();
        excel.create();
        excel.createSheet("Result");
        datas.forEach((data) -> {
            excel.append(
                    data.getSubject(),
                    data.getAuthor(),
                    data.getFrom(),
                    data.getPublishedDate());
        });
        excel.save(filePath);
    }
}
