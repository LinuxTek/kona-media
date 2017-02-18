/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.media.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import org.imgscalr.Scalr;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.linuxtek.kona.media.model.KImage;
import com.linuxtek.kona.media.model.KOrientation;
import com.linuxtek.kona.util.KFileUtil;

import net.coobird.thumbnailator.Thumbnails;

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

    public static byte[] toByteArray(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        ImageIO.write(image, formatName, os);
        byte[] data = os.toByteArray();
        os.close();

        return data;
    }
    
    public static KImage toImage(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        ImageIO.write(image, formatName, os);
        byte[] data = os.toByteArray();
        os.close();

        return toImage(data);
    }

    // http://stackoverflow.com/questions/5905868/how-to-rotate-jpeg-images-based-on-the-orientation-metadata
    // https://github.com/coobird/thumbnailator
    /*
    public static BufferedImage getRotatedImage(BufferedImage image) throws IOException {
        //return Thumbnails.of(image).scale(1).asBufferedImage();
        Metadata metadata = ImageMetadataReader.readMetadata(imagePath);
    }
    */
    
    // http://chunter.tistory.com/143
    // http://stackoverflow.com/questions/5905868/how-to-rotate-jpeg-images-based-on-the-orientation-metadata
    public static AffineTransform getExifTransformation(KImage image) {
        
        if (image == null || image.getOrientation() == null) {
            return null;
        }

        AffineTransform t = new AffineTransform();

        switch (image.getOrientation().value()) {
            case 1:
                break;

            case 2: // Flip X
                t.scale(-1.0, 1.0);
                t.translate(-image.getWidth(), 0);
                break;

            case 3: // PI rotation 
                t.translate(image.getWidth(), image.getHeight());
                t.rotate(Math.PI);
                break;

            case 4: // Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -image.getHeight());
                break;

            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;

            case 6: // -PI/2 and -width
                t.translate(image.getHeight(), 0);
                t.rotate(Math.PI / 2);
                break;

            case 7: // PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-image.getHeight(), 0);
                t.translate(0, image.getWidth());
                t.rotate(  3 * Math.PI / 2);
                break;

            case 8: // PI / 2
                t.translate(0, image.getWidth());
                t.rotate(  3 * Math.PI / 2);
                break;
        }

        return t;
    }
    
    
    protected static BufferedImage createOptimalImage(BufferedImage src, int width, int height) 
            throws IllegalArgumentException {

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width [" + width
                    + "] and height [" + height + "] must be >= 0");
        }

        return new BufferedImage(width, height,
                (src.getTransparency() == Transparency.OPAQUE 
                ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB));
    }

    public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) {
        AffineTransformOp op = new AffineTransformOp(transform, null);
        BufferedImage destImage = op.createCompatibleDestImage(image, image.getColorModel());
        Graphics2D g2d = destImage.createGraphics();
        g2d.drawImage(image, transform, null);
        g2d.dispose();
        return destImage;
        
        /*
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);
        return destinationImage;
        */
    }


    public static BufferedImage transformImage2(BufferedImage image, AffineTransform transform) {


        /*
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

        //BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? null : image.getColorModel());
        BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? null : image.getColorModel());

        Graphics2D g = destinationImage.createGraphics();

g.setBackground(Color.WHITE);
        //Color transparent = new Color(0,0,0,0);
        //g.setBackground(transparent);

g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        */
        
        /*
        BufferedImage destinationImage = scaleCanvas(image, image.getWidth(), image.getHeight());

        destinationImage = op.filter(image, destinationImage);

        return destinationImage;
        */
        
            /*
        int type = (image.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        
        BufferedImage after = new BufferedImage(image.getWidth(), image.getHeight(), type);
        */
        
        /*
        BufferedImage after = op.filter(image, null);
        return after;
        */
        
        // Create our target image we will render the rotated result to.
        BufferedImage result = createOptimalImage(image, image.getWidth(), image.getHeight());
        Graphics2D g2d = (Graphics2D) result.createGraphics();

        /*
         * Render the resultant image to our new rotatedImage buffer, applying
         * the AffineTransform that we calculated above during rendering so the
         * pixels from the old position are transposed to the new positions in
         * the resulting image correctly.
         */
        g2d.drawImage(image, transform, null);
        g2d.dispose();
        
        return result;
    }
 
    
    public static KImage getNormalizedImage(byte[] data) throws IOException {

        KImage result = toImage(data);

        AffineTransform transform = getExifTransformation(result);

        if (transform != null) {
            String formatName = getFormatName(data);

            ByteArrayInputStream in = new ByteArrayInputStream(data);

            BufferedImage image = ImageIO.read(in);

            image = transformImage(image, transform);

            result = toImage(image, formatName);
        }

        return result;
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

    public static BufferedImage scaleCanvas(BufferedImage source,
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

    // return data, width, height
    public static KImage resize(byte[] src, int targetWidth, int targetHeight) throws IOException {
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
        
        return toImage(resultBytes);
    }
    
 
	public static KImage resizeToMaxWidthAndHeight(byte[] src, 
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
        
		KImage image = null;

		if (newWidth>0 && newHeight>0) {
			image = resize(src, newWidth, newHeight);
		} else {
		    image = toImage(src);
		}
        
        logger.debug("resizeToMaxWidthAndHeight: " 
        		+ "\nwidth: " + width
        		+ "\nheight: " + height
        		+ "\nnewWidth: " + image.getWidth()
        		+ "\nnewHeight: " + image.getHeight());
        
        return image;
	}
    
    public static KImage toImage(byte[] data) throws IOException {
        KImage image = new KImage();

        KImageInfo info = new KImageInfo(data);
        
        image.setData(data);
        image.setWidth(info.getWidth());
        image.setHeight(info.getHeight());
        image.setBitsPerPixel(info.getBitsPerPixel());
        image.setContentType(info.getMimeType());
        image.setSize(Long.valueOf(data.length));


        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(in);

            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null) {
                KOrientation orientation = 
                        KOrientation.getInstance(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));

                image.setOrientation(orientation);
            }

            JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

            if (jpegDirectory != null) {


                Integer width = jpegDirectory.getImageWidth();
                Integer height = jpegDirectory.getImageHeight();

                if (image.getWidth() == null || !image.getWidth().equals(width)) {
                    image.setWidth(width);
                }

                if (image.getHeight() == null || !image.getHeight().equals(height)) {
                    image.setHeight(height);
                }
            }
        } catch (MetadataException | ImageProcessingException e) {
            logger.warn("Could not get orientation");
        }

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

	public static KImage crop(byte[] data, int x, int y, int w, int h) 
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

		data = os.toByteArray();
		
		return new KImage(data, contentType, w, h);
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

