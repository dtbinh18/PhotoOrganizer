/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hoamua.photoorganizer;

import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.MetadataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;

/**
 * Photo object
 * 
 * @author daothanhbinh
 */
public class Media {

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
    
    /**
     * Constructor
     *
     * @param photoDir
     * @param thumpDir
     * @param originalFilePath
     */
    public Media(String photoDir, String originalFilePath) {
        this.photoDir = photoDir;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
     * Read exif metadata and set YEAR, DATE, WIDTH, HEIGHT
     *
     * @throws IOException
     * @throws MetadataException
     * @throws ImageProcessingException
     * @throws NullPointerException
     */
    private void readMetadata() throws IOException, NullPointerException {
        File file = new File(getOriginalFilePath());

        // Set YEAR/DATE
        long date = file.lastModified();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        this.year = simpleDateFormat.format(date);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.date = simpleDateFormat.format(date);

        // Get type
        this.type = getMediaType(file);

        //printMetadata(getOriginalFilePath());
    }

    /**
     * Copy image
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    public void move(File src, File dst) throws IOException {
        // Overwite
        if (src.exists() && dst.exists() && isDuplicated(src, dst)) {
            logger.log(Level.WARNING, "DUPLICATED " + src.getAbsolutePath() + " " + dst.getAbsolutePath());
            return;
        }

        // use rename method first. if false, copy file.
        if (src.renameTo(dst)) {
            logger.log(Level.WARNING, "MOVED " + src.getAbsolutePath() + " " + dst.getAbsolutePath());
            return;
        }

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        if (dst.exists()) {
            src.delete();
        }
        logger.log(Level.WARNING, "COPIED " + src.getAbsolutePath() + " " + dst.getAbsolutePath());
    }

    /**
     * Compare image by md5
     *
     * @param image1
     * @param image2
     * @return
     */
    private boolean isDuplicated(File file1, File file2) {
        String digest1 = getDigest(file1);
        String digest2 = getDigest(file2);
        if (!digest1.equals(digest2)) {
            return false;
        } else {
            logger.log(Level.INFO, "Duplicated,{0},{1},{2}",
                    new String[]{file1.getAbsolutePath(), file2.getAbsolutePath(), digest1});
            return true;
        }

    }

    /**
     * Calculate MD5 hash
     *
     * @param file
     * @return
     */
    private String getDigest(File file) {
        try {
            if (md == null) {
                md = MessageDigest.getInstance("MD5");
            } else {
                md.reset();
            }

            InputStream fin = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int numBytes;
            while ((numBytes = fin.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            String result = new String(Hex.encodeHex(digest));

            fin.close();

            return result;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @NotNull
    private String getMediaType(@NotNull File file) throws IOException {
        List<String> extensions = Arrays.asList(new String[] {".mov",".avi",".flv",".mp4",".mpg",".mpeg"});
        String ext = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
        if (extensions.contains(ext)) {
            return ext;
        } else {
            throw new IOException("File format is not supported");
        }
    }
}
