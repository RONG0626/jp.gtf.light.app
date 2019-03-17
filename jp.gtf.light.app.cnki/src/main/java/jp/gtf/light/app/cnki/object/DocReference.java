/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.gtf.light.app.cnki.object;

import lombok.Getter;
import lombok.Setter;

/**
 * 参考文献データ
 *
 * @author fuukyoutou
 */
@Getter
@Setter
public class DocReference {

    /**
     * タイトル
     */
    private String subject;
    /**
     * 作成者
     */
    private String author;
    /**
     * ソース
     */
    private String from;
    /**
     * 発表日時
     */
    private String publishedDate;

}
