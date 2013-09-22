package com.hoamua.photoorganizer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <pre>
 *
 * PhotoOrganizer helps to re-organize your photos.
 * All photos in a specified directory will be arranged by the date the photo was taken.
 *
 * Move photo to photoDir, under a specific directory
 *  photoDir
 *       |__ {YEAR}
 *              |__ {DATE}
 *                     |__ {Organized photos}
 * </pre>
 *
 * @author daothanhbinh
 */
public class PhotoOrganizer {

    private static final Logger logger = Logger.getLogger(PhotoOrganizer.class.getName());
    /**
     * Directory where original photos are staying in.
     */
    String fromDir;
    /**
     * Directory where original photos will be moved to.
     */
    String photoDir;
    /**
     * Recursively work with files under sub directories or not.
     */
    boolean recursive;
    /**
     * Directory where thump photos will be created in.
     */
    String thumpDir;
    /**
     * Flag to show that if you want to create thump photos.
     */
    boolean thumpCreation;
    /**
     * Max Width/Height size of thump photos.
     * The scale will be kept.
     */
    int thumpMaxWidthHeight;
    

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        PhotoOrganizer po = new PhotoOrganizer();
        po.run();
    }

    /**
     *
     */
    public void run() {
        fromDir = PhotoConfig.getString("photo_from_dir", "./");
        photoDir = PhotoConfig.getString("photo_to_dir", "./");
        recursive = PhotoConfig.getBool("from_dir_recursive", false);
        thumpDir = PhotoConfig.getString("photo_thump_dir", "./Thumbs");
        thumpCreation = PhotoConfig.getBool("photo_thump_create", false);
        thumpMaxWidthHeight = PhotoConfig.getInt("photo_thump_max_width_height", 150);

        logger.log(Level.INFO, "photo_from_dir = {0}", fromDir);
        logger.log(Level.INFO, "photo_to_dir = {0}", photoDir);
        logger.log(Level.INFO, "photo_thump_dir = {0}", thumpDir);
        logger.log(Level.INFO, "photo_thump_create = {0}", thumpCreation);
        logger.log(Level.INFO, "photo_thump_max_width_height = {0}", thumpMaxWidthHeight);

        if (fromDir == null || photoDir == null || thumpDir == null) {
            logger.log(Level.SEVERE, "Configuration error. Directory configuration is not enough");
            System.exit(1);
        }

        organizeFileInDir(new File(fromDir), recursive);
    }

    /**
     * Organize file in specified directory
     * 
     * @param dir 
     */
    private void organizeFileInDir(File dir, boolean recursive) {
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                orginizeFile(entry);
            } else if (entry.isDirectory() && recursive) {
                organizeFileInDir(entry, recursive);
            }
        }
    }

    /**
     * Organize a specified file
     *
     * @param entry
     * @param photoDir
     * @param thumpDir
     * @param thumpCreation
     * @param thumpMaxWidthHeight
     */
    private void orginizeFile(File file) {
        logger.log(Level.INFO, "START, {0}", file.getAbsolutePath());
        
        // organize image file
        Photo p = new Photo(photoDir, thumpDir, file.getAbsolutePath());
        boolean result = p.organizePhoto();
        if (result && thumpCreation) {
            p.createThump(thumpMaxWidthHeight);
        }
        if (result) {
            return;
        }
        logger.log(Level.INFO, "FINISH, {0}", file.getAbsolutePath());
        
        // oranize other files (movies)
        Video m = new Video(photoDir, file.getAbsolutePath());
        m.organizePhoto();
    }
}
