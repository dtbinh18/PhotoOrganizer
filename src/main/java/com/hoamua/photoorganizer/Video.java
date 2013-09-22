/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hoamua.photoorganizer;

import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.MetadataException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Photo object
 * 
 * @author daothanhbinh
 */
public class Video extends MediaAbstract{
    private static final Logger logger = Logger.getLogger(Photo.class.getName());

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
    public Video(String photoDir, String originalFilePath) {
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
