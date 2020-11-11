package com.bajins.demo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * org.apache.commons.commons-io
 */
public class CommonsIO {
    public static void main(String[] args) throws IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");// 系统默认缓存目录
        File file = new File(tmpdir + "/test");
        if (!file.exists()) {
            file.mkdirs();
        }
        FileUtils.getFile();
        //FileUtils.getFile();
        //FileUtils.getTempDirectoryPath();
        //FileUtils.getTempDirectory();
        //FileUtils.getUserDirectoryPath();
        //FileUtils.getUserDirectory();
        //FileUtils.openInputStream();
        //FileUtils.openOutputStream();
        //FileUtils.openOutputStream();
        //FileUtils.byteCountToDisplaySize();
        //FileUtils.byteCountToDisplaySize();
        //FileUtils.touch();
        //FileUtils.convertFileCollectionToFileArray();
        //FileUtils.listFiles();
        //FileUtils.listFilesAndDirs();
        //FileUtils.iterateFiles();
        //FileUtils.iterateFilesAndDirs();
        //FileUtils.listFiles();
        //FileUtils.iterateFiles();
        //FileUtils.contentEquals();
        //FileUtils.contentEqualsIgnoreEOL();
        //FileUtils.toFile();
        //FileUtils.toFiles();
        //FileUtils.toURLs();
        //FileUtils.copyFileToDirectory();
        //FileUtils.copyFileToDirectory();
        //FileUtils.copyFile();
        //FileUtils.copyFile();
        //FileUtils.copyFile();
        //FileUtils.copyDirectoryToDirectory();
        //FileUtils.copyDirectory();
        //FileUtils.copyDirectory();
        //FileUtils.copyDirectory();
        //FileUtils.copyDirectory();
        //FileUtils.copyURLToFile();
        //FileUtils.copyURLToFile();
        //FileUtils.copyInputStreamToFile();
        FileUtils.deleteDirectory(file.getAbsoluteFile());
        //FileUtils.deleteQuietly();
        //FileUtils.directoryContains();
        //FileUtils.cleanDirectory();
        //FileUtils.waitFor();
        //FileUtils.readFileToString();
        //FileUtils.readFileToString();
        //FileUtils.readFileToString();
        //FileUtils.readFileToByteArray();
        //FileUtils.readLines();
        //FileUtils.readLines();
        //FileUtils.readLines();
        //FileUtils.lineIterator();
        //FileUtils.lineIterator();
        //FileUtils.writeStringToFile();
        //FileUtils.writeStringToFile();
        //FileUtils.writeStringToFile();
        //FileUtils.writeStringToFile();
        //FileUtils.writeStringToFile();
        //FileUtils.writeStringToFile();
        //FileUtils.write();
        //FileUtils.write();
        //FileUtils.write();
        //FileUtils.write();
        //FileUtils.write();
        //FileUtils.write();
        //FileUtils.writeByteArrayToFile();
        //FileUtils.writeByteArrayToFile();
        //FileUtils.writeByteArrayToFile();
        //FileUtils.writeByteArrayToFile();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.writeLines();
        //FileUtils.forceDelete();
        //FileUtils.forceDeleteOnExit();
        //FileUtils.forceMkdir();
        //org.apache.tomcat.util.http.fileupload.FileUtils.forceMkdirParent();
        //FileUtils.sizeOf();
        //FileUtils.sizeOfDirectory();
        //FileUtils.isFileNewer();
        //FileUtils.isFileNewer();
        //FileUtils.isFileNewer();
        //FileUtils.isFileOlder();
        //FileUtils.isFileOlder();
        //FileUtils.isFileOlder();
        //FileUtils.checksumCRC32();
        //FileUtils.checksum();
        //FileUtils.moveDirectory();
        //FileUtils.moveDirectoryToDirectory();
        //FileUtils.moveFile();
        //FileUtils.moveFileToDirectory();
        //FileUtils.moveToDirectory();
        //FileUtils.isSymlink();
    }
}
