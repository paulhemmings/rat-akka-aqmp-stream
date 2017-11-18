package com.razor.raas.storage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paul.hemmings on 2/15/16.
 * Wrapper around an array of simple key,value maps
 */

public class ContentDocument extends ArrayList<ContentDocument.ContentRow> {

    public static class ContentRow extends HashMap<String, Object> {

        public ContentRow add(String key, Object value) {
            super.put(key, value);
            return this;
        }
    }

    public ContentRow createRow() {
        ContentRow contentRow = new ContentRow();
        this.add(contentRow);
        return contentRow;
    }

}
