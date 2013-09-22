/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hoamua.photoorganizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author daothanhbinh
 */
public class MediaAbstract {
    private static final Logger logger = Logger.getLogger(Photo.class.getName());
    private static MessageDigest md = null;
    
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
            src.delete();
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
}
