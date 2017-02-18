/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.media.model;

public class KImage extends KMedia {
    private static final long serialVersionUID = 1L;

    private Integer width;
    private Integer height;
    private Integer bitsPerPixel;
    private KOrientation orientation;

    public KImage() {
    }
    
    public KImage(byte[] data, String contentType, Integer width, Integer height) {
        super(data, contentType);
        this.width = width;
        this.height = height;
    }

    public KImage(byte[] data, String contentType, Integer width, Integer height, Integer bitsPerPixel, KOrientation orientation) {
        this(data, contentType, width, height);
        this.bitsPerPixel = bitsPerPixel;
        this.orientation = orientation;
    }

    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }
    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }
    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }
    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }
    /**
     * @return the bitsPerPixel
     */
    public Integer getBitsPerPixel() {
        return bitsPerPixel;
    }
    /**
     * @param bitsPerPixel the bitsPerPixel to set
     */
    public void setBitsPerPixel(Integer bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
    }
    /**
     * @return the orientation
     */
    public KOrientation getOrientation() {
        return orientation;
    }
    /**
     * @param orientation the orientation to set
     */
    public void setOrientation(KOrientation orientation) {
        this.orientation = orientation;
    }
}
