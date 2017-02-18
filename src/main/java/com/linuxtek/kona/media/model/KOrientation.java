/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.media.model;


/*
 * http://www.impulseadventure.com/photo/exif-orientation.html
 *

EXIF Orientation Value  Row #0 is:  Column #0 is:
1   Top Left side
2*  Top Right side
3   Bottom  Right side
4*  Bottom  Left side
5*  Left side   Top
6   Right side  Top
7*  Right side  Bottom
8   Left side   Bottom
NOTE: Values with "*" are uncommon since they represent "flipped" orientations.
*/

public enum KOrientation {
    TOP_LEFT(1),
    TOP_RIGHT(2),
    BOTTOM_RIGHT(3),
    BOTTOM_LEFT(4),
    LEFT_TOP(5),
    RIGHT_TOP(6),
    RIGHT_BOTTOM(7),
    LEFT_BOTTOM(8);
    
    private Integer value;

    private KOrientation(Integer value) {
        this.value = value;
    }
    
    public Integer value() {
        return value;
    }
    
    public static KOrientation getInstance(Integer value) {
        KOrientation[] values = KOrientation.values();

        for (KOrientation type : values) {
            if (type.value().equals(value)) {
                return (type);
            }
        }

        throw new IllegalArgumentException("ERROR: Orientation: value not found: " + value);
    }
}

