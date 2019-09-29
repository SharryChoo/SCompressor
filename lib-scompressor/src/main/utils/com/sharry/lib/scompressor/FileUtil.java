package com.sharry.lib.scompressor;

import java.io.File;
import java.io.IOException;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-29 14:07
 */
final class FileUtil {

    private static final String SUFFIX_JPEG = ".jpg";
    private static final String UNSUSPECTED_FILE_PREFIX = "SCompressor_";

    static File createUnsuspectedFile() throws IOException {
        File tempFile = new File(
                Preconditions.checkNotNull(SCompressor.sUsableDir, "If U not set output path, " +
                        "Please invoke SCompressor.init config an usable directory."),
                UNSUSPECTED_FILE_PREFIX + System.currentTimeMillis() + SUFFIX_JPEG
        );
        if (tempFile.exists()) {
            tempFile.delete();
        }
        tempFile.createNewFile();
        return tempFile;
    }

    static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        file.createNewFile();
        return file;
    }

}
