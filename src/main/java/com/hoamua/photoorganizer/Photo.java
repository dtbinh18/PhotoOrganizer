/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hoamua.photoorganizer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Hex;
import org.imgscalr.Scalr;

/**
 * Photo object
 * 
 * @author daothanhbinh
 */
public class Photo  extends MediaAbstract{

    private static final Logger logger = Logger.getLogger(Photo.class.getName());
    private static MessageDigest md = null;
    /**
     * Absolute file path of original image file
     */
    private String originalFilePath;
    /**
     * Target photo directory
     */
    private String photoDir;
    /**
     * Thump photo directory
     */
    private String thumpDir;
    /**
     * Original image width
     */
    private int width;
    /**
     * Original image height
     */
    private int height;
    /**
     * Image type
     */
    private String type;
    /**
     * File path of file after being moved
     */
    private String filePath;
    /**
     * YEAR
     */
    private String year;
    /**
     * DATE
     */
    private String date;
    /**
     * Image types
     */
    private static final int JPEG_FILE_MAGIC_NUMBER = 0xFFD8;
    private static final int MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D;  // "MM"
    private static final int INTEL_TIFF_MAGIC_NUMBER = 0x4949;     // "II"
    private static final int PSD_MAGIC_NUMBER = 0x3842;            // "8B" // TODO the full magic number is 8BPS
    private static final int PNG_MAGIC_NUMBER = 0x8950;            // "?P" // TODO the full magic number is six bytes long
    private static final int BMP_MAGIC_NUMBER = 0x424D;            // "BM" // TODO technically there are other very rare magic numbers for OS/2 BMP files...
    private static final int GIF_MAGIC_NUMBER = 0x4749;            // "GI" // TODO the full magic number is GIF or possibly GIF89a/GIF87a

    /**
     * Constructor
     *
     * @param photoDir
     * @param thumpDir
     * @param originalFilePath
     */
    public Photo(String photoDir, String thumpDir, String originalFilePath) {
        this.photoDir = photoDir;
        this.thumpDir = thumpDir;
        this.originalFilePath = originalFilePath;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public String getPhotoDir() {
        return photoDir;
    }

    public void setPhotoDir(String photoDir) {
        this.photoDir = photoDir;
    }

    public String getThumpDir() {
        return thumpDir;
    }

    public void setThumpDir(String thumpDir) {
        this.thumpDir = thumpDir;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumpFilePath() {
        return filePath.replace(photoDir, thumpDir);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * <pre>
     * Move photo to photoDir, under a specific directory
     *     photoDir
     *         |__ {YEAR}
     *                |__ {DATE}
     *                       |__ {Organized photos}
     * </pre>
     */
    public boolean organizePhoto() {
        try {
            readMetadata();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot read file, " + getOriginalFilePath(), ex);
            return false;
        } catch (MetadataException ex) {
            logger.log(Level.SEVERE, "Cannot read metadata, " + getOriginalFilePath(), ex);
            return false;
        } catch (ImageProcessingException ex) {
            logger.log(Level.SEVERE, "File format is not supported, " + getOriginalFilePath(), ex);
            return false;
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, "Cannot read metadata properties, " + getOriginalFilePath(), ex);
            return false;
        }

        // Move file
        File curFile = new File(originalFilePath);
        String targetDir = this.photoDir + File.separator
                + this.year + File.separator
                + this.date;
        File newFile = new File(targetDir, curFile.getName());
        newFile.getParentFile().mkdirs();

        logger.log(Level.INFO, "Move File,{0},{1}",
                new String[]{curFile.getAbsolutePath(), newFile.getAbsolutePath()});
        try {
            move(curFile, newFile);
            filePath = newFile.getAbsolutePath();
        } catch (IOException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    /**
     * Create thump image with specified width and height
     *
     * @param newWidth
     * @param newHeight
     */
    public boolean createThump(int maxWidthHeight) {
        try {
            File image = new File(getFilePath());
            File thump = new File(getThumpFilePath());

            BufferedImage in = ImageIO.read(image);
            BufferedImage out = Scalr.resize(in, maxWidthHeight);
            
            // Create parrent directory if it does not exist
            if (!thump.getParentFile().isDirectory()) {
                thump.getParentFile().mkdirs();
            }
            // Overwrite existing thump image
            if (thump.exists()) {
                thump.delete();
            }
            boolean result = ImageIO.write(out, getType(), thump);

            if (result) {
                logger.log(Level.INFO, "Create thump OK, {0}", thump.getAbsolutePath());
            } else {
                logger.log(Level.INFO, "Create thump NG, {0}", thump.getAbsolutePath());
            }
            return result;
        } catch (IOException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Read exif metadata and set YEAR, DATE, WIDTH, HEIGHT
     *
     * @throws IOException
     * @throws MetadataException
     * @throws ImageProcessingException
     * @throws NullPointerException
     */
    private void readMetadata() throws IOException, MetadataException, ImageProcessingException, NullPointerException {
        File imageFile = new File(getOriginalFilePath());

        // Read exif
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        // obtain the Exif directory
        ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
        if (directory == null) {
            throw new NullPointerException();
        }
        
        // Set YEAR/DATE
        Date dateDetail = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        this.year = simpleDateFormat.format(dateDetail);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.date = simpleDateFormat.format(dateDetail);

        // Get original width/height
        this.width = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
        this.height = directory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);

        // Get type
        this.type = getImageType(imageFile);

        //printMetadata(getOriginalFilePath());
    }

    @NotNull
    private String getImageType(@NotNull File file) throws ImageProcessingException, IOException {
        InputStream inputStream = new FileInputStream(file);
        try {
            return getImageType(inputStream);
        } finally {
            inputStream.close();
        }
    }

    /**
     * Retrieve image type
     *
     * @param inputStream
     * @return
     * @throws ImageProcessingException
     * @throws IOException
     */
    @NotNull
    private String getImageType(@NotNull InputStream inputStream) throws ImageProcessingException, IOException {
        InputStream bufferedInputStream = new BufferedInputStream(inputStream);

        int magicNumber = peekMagicNumber(bufferedInputStream);

        if (magicNumber == -1) {
            throw new ImageProcessingException("Could not determine file's magic number.");
        }

        // This covers all JPEG files
        if ((magicNumber & JPEG_FILE_MAGIC_NUMBER) == JPEG_FILE_MAGIC_NUMBER) {
            return "jpg";
        }

        // This covers all TIFF and camera RAW files
        if (magicNumber == INTEL_TIFF_MAGIC_NUMBER || magicNumber == MOTOROLA_TIFF_MAGIC_NUMBER) {
            return "tiff";
        }

        // This covers PSD files
        // TODO we should really check all 4 bytes of the PSD magic number
        if (magicNumber == PSD_MAGIC_NUMBER) {
            return "psd";
        }

        // This covers BMP files
        if (magicNumber == PNG_MAGIC_NUMBER) {
            return "png";
        }

        // This covers BMP files
        if (magicNumber == BMP_MAGIC_NUMBER) {
            return "bmp";
        }

        // This covers GIF files
        if (magicNumber == GIF_MAGIC_NUMBER) {
            return "gif";
        }

        throw new ImageProcessingException("File format is not supported");
    }

    /**
     * Reads the first two bytes from
     * <code>inputStream</code>, then rewinds.
     */
    private int peekMagicNumber(@NotNull InputStream inputStream) throws IOException {
        inputStream.mark(2);
        final int byte1 = inputStream.read();
        final int byte2 = inputStream.read();
        inputStream.reset();

        if (byte1 == -1 || byte2 == -1) {
            return -1;
        }

        return byte1 << 8 | byte2;
    }

    /**
     * Print exif metadata of an image
     *
     * @param image
     */
    private void printMetadata(String file) {
        printMetadata(new File(file));
    }
    
    private void printMetadata(File image) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(image);
            StringBuilder meta = new StringBuilder();
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    meta.append(tag.toString()).append(System.getProperty("line.separator"));
                }
            }
            logger.log(Level.INFO, meta.toString());
        } catch (ImageProcessingException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
