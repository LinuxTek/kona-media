/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.media.model;

import java.io.Serializable;

import com.linuxtek.kona.util.KClassUtil;

public class KMedia implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] data;
    private String contentType;
    private Long size;
    
    public KMedia() {
        
    }
    
    public KMedia(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
        this.size = Long.valueOf(data.length);
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }
    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
    /**
     * @return the size
     */
    public Long getSize() {
        return size;
    }
    /**
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }
    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    @Override
    public String toString() {
        return KClassUtil.toString(this);
    }
}
