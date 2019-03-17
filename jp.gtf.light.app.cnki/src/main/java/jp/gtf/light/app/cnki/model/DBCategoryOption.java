/*
 * © 2019 Gtf Software, Inc. All Rights Reserved
 * Mail fxd@hotmail.co.jp
 * More information please visit https://gtf.jp
 */
package jp.gtf.light.app.cnki.model;

import lombok.Getter;

/**
 * 検索カテゴリ
 *
 * @author F
 */
@Getter
public class DBCategoryOption {

    private final String value;
    private final String label;

    /**
     * CONSTRUCT METHOD
     *
     * @param value 値
     * @param label ラベル
     */
    public DBCategoryOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static final DBCategoryOption SCDB = new DBCategoryOption("SCDB", "文献");
    public static final DBCategoryOption CJFQ = new DBCategoryOption("CJFQ", "期刊");
    public static final DBCategoryOption CDMD = new DBCategoryOption("CDMD", "博硕士");

}
