/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.media.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import com.linuxtek.kona.util.KFileUtil;

public class KImageUtil {
    private static Logger logger = Logger.getLogger(KImageUtil.class);

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(
            BufferedImage img,
            int targetWidth,
            int targetHeight) {
        return (getScaledInstance(img, targetWidth, targetHeight,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC, true));
    } 



    public static BufferedImage getScaledInstance(
            BufferedImage img,
            int targetWidth,
            int targetHeight,
            Object hint,
            boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = (BufferedImage)img;

        int w, h;

        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    public BufferedImage scaleCanvas(BufferedImage source,
            Integer width, Integer height) {
        logger.debug("scaling canvas to: " + width + "x" + height);

        int type = (source.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage tmp = new BufferedImage(width, height, type);

        Graphics2D g2 = tmp.createGraphics();

        //Make background white
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Color fillerColor = new Color(0,0,0,0);
        g2.setPaint(fillerColor); //Make area behind image transparent

        int adjX = ( (width - source.getWidth()) / 2 );
        int adjY = ( (height - source.getHeight()) / 2 );

        try {
            g2.setPaint(fillerColor); //Make area behind image transparent 
            g2.drawImage(source, adjX, adjY, source.getWidth(),
                source.getHeight(), fillerColor, null);
        } catch(Throwable t) {
            logger.error("Unable to scale canvas: " + t, t);
        } finally {
            g2.dispose();
        }
        return (tmp);
    }

    public static BufferedImage resize(BufferedImage src, int targetWidth, 
            int targetHeight) throws IOException {
        return Scalr.resize(src, targetWidth, targetHeight, (BufferedImageOp[])null); 
    }

    public static byte[] resize(byte[] src, int targetWidth, 
            int targetHeight) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(src);

        BufferedImage image = ImageIO.read(is);

        if (image == null) {
            logger.error("ImageIO.read failed.");
        }

        BufferedImage result = resize(image, targetWidth, targetHeight);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        String formatName = getFormatName(src);
        ImageIO.write(result, formatName, os);
        byte[] resultBytes = os.toByteArray();

        is.close();
        os.close();
        return (resultBytes);
    }
    
    public static class Image implements Serializable {
		private static final long serialVersionUID = 2475935877346904537L;
		public byte[] data;
    	public Integer width;
    	public Integer height;
    	public Long size;
    	public Integer bitsPerPixel;
    	public String contentType;
    }
    
	public static Image resizeToMaxWidthAndHeight(byte[] src, 
			int maxWidth, int maxHeight ) throws IOException {
		logger.debug("resizeToMaxWidthAndHeight: maxWidth: " + maxWidth + "  maxHeight: " + maxHeight);
        
        Integer widthDiff = -1;
        Integer heightDiff = -1;
        
        Integer newWidth = -1;
        Integer newHeight = -1;
        
        Map<String,Object> info = getImageInfo(src);
        Integer width = (Integer) info.get("width");
        Integer height = (Integer) info.get("height");
        
		logger.debug("resizeToMaxWidthAndHeight: probed width: " + width + "  probed height: " + height);
        
		if (maxWidth > 0 && width > maxWidth) {
            widthDiff = width - maxWidth;
		}
        
		if (maxHeight > 0 && height > maxHeight) {
			heightDiff = height - maxHeight;
		}
        
		if (widthDiff > 0 && widthDiff >= heightDiff) {
            newWidth = maxWidth;
            float ratio = Float.valueOf(maxWidth) / Float.valueOf(width);
            newHeight = (int)(height * ratio);
		} else if (heightDiff > 0 && heightDiff >= widthDiff) {
            newHeight = maxHeight; 
            float ratio = Float.valueOf(maxHeight) / Float.valueOf(height);
            newWidth = (int)(width * ratio);
		}
        
		if (newWidth>0 && newHeight>0) {
			src = resize(src, newWidth, newHeight);
            /*
            file.setData(bytes);
            file.setWidth(newWidth);
            file.setHeight(newHeight);
            file.setSize(Long.valueOf(bytes.length));
            */
		}
        
        logger.debug("resizeToMaxWidthAndHeight: " 
        		+ "\nwidth: " + width
        		+ "\nheight: " + height
        		+ "\nnewWidth: " + newWidth
        		+ "\nnewHeight: " + newHeight);
        
        return toImage(src);
	}
    
    public static Image toImage(byte[] data) throws IOException {
        Image image = new Image();
        KImageInfo info = new KImageInfo(data);
        image.data = data;
        image.width = info.getWidth();
        image.height = info.getHeight();
        image.bitsPerPixel = info.getBitsPerPixel();
        image.contentType = info.getMimeType();
        image.size = Long.valueOf(data.length);
        return image;
    }
    
    public static Map<String,Object> getImageInfo(String filePath) 
            throws IOException {
    	byte[] data = KFileUtil.toByteArray(filePath);
    	return getImageInfo(data);
    }

    public static Map<String,Object> getImageInfo(byte[] data) 
            throws IOException {
        HashMap<String,Object> map = new HashMap<String,Object>();

        KImageInfo info = new KImageInfo(data);
        Integer width = info.getWidth();
        Integer height = info.getHeight();
        Integer bits = info.getBitsPerPixel();
        String contentType = info.getMimeType();

        	/*
        map.put("width", width == null ? null : width.toString());
        map.put("height", height == null ? null : height.toString());
        map.put("bitsPerPixel", bits == null ? null : bits.toString());
        */
        
        map.put("width", width);
        map.put("height", height);
        map.put("bitsPerPixel", bits);
        map.put("contentType", contentType);

        return map;
    }

    /*
    public static void updateImageInfo(KBaseFile file) throws IOException {
        byte[] data = file.getData();
        if (data == null) {
            logger.info("file data is null");
            return;
        }

        KImageInfo info = new KImageInfo(data);
        file.setWidth(info.getWidth());
        file.setHeight(info.getHeight());
        file.setBitsPerPixel(info.getBitsPerPixel());
        
        long dataSize = Long.valueOf(data.length);
        if (file.getSize() == null || !file.getSize().equals(dataSize)) {
        	logger.warn("setting file size to: " 
        			+ dataSize + "  old value: " + file.getSize());
            file.setSize(dataSize);
        }

        String contentType = info.getMimeType();
        if (file.getContentType() == null) {
            file.setContentType(contentType);
        }

        //sanity check
        if (!contentType.equalsIgnoreCase(file.getContentType())) {
            logger.info("Content-type mismatch:"
                + "\n\tfile id: " + file.getId()
                + "\n\tfile name: " + file.getName()
                + "\n\tfile content-type: " + file.getContentType()
                + "\n\tKImageInfo content-type: " + contentType);
        }

        logger.debug("Image info: " + file.getName()
                + "\n\twidth: " + file.getWidth()
                + "\n\theight: " + file.getHeight()
                + "\n\tbitsPerPixel: " + file.getBitsPerPixel());
    }
    */

    // Returns the format name of the image in the object 'o'.
    // 'o' can be either a File or InputStream object.
    // Returns null if the format is not known.
    public static String getFormatName(byte[] data) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        return getFormatName(is);
    }

    private static String getFormatName(Object o) {
        String formatName = null;

        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(o);

            // Find all image readers that recognize the image format
            Iterator<?> iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                // No readers found
                logger.info("No ImageReaders found for InputStream.");
                return null;
            }

            // Use the first reader
            ImageReader reader = (ImageReader)iter.next();

            // Close stream
            iis.close();

            // Return the format name
            formatName = reader.getFormatName();
        } catch (IOException e) {
            logger.error(e);
        }

        logger.debug("format name: " + formatName);
        return formatName;
    }

    public static String getContentType(byte[] data) throws IOException {
        KImageInfo info = new KImageInfo(data);
        return (info.getMimeType());
    }

    public static String getContentTypeFileExtension(String contentType) {
        String formatName = "jpg";
        if (contentType.toLowerCase().endsWith("png")) {
            formatName = "png";
        } else if (contentType.toLowerCase().endsWith("gif")) {
            formatName = "gif";
        } else if (contentType.toLowerCase().endsWith("tif")) {
            formatName = "tif";
        }

        return formatName;
    }

	public static byte[] crop(byte[] data, int x, int y, int w, int h) 
			throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		BufferedImage image = ImageIO.read(is);
		BufferedImage out = null;
		out = image.getSubimage(x, y, w, h);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		//preserve the original content type
		String contentType = getContentType(data);
		String formatName = getContentTypeFileExtension(contentType);

		ImageIO.write(out, formatName, os);

		return os.toByteArray();
	}
    
    /*
    public static boolean isImage(KFile file) {
        return isImage(file.getData());
    }
    */

    public static boolean isImage(byte[] data) {
        if (data == null) {
            logger.debug("isImage: data is null; return false");
            return false;
        }

        try {
            KImageInfo info = new KImageInfo(data);
            logger.debug("isImage: " + info.getMimeType());
            return true;
        } catch (Exception e) {
            logger.debug("isImage: return false: " + e.getMessage());
            return false;
        }
    }
}
