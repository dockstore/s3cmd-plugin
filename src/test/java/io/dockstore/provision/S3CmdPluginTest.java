package io.dockstore.provision;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author gluu
 * @since 07/03/17
 */
public class S3CmdPluginTest {
    S3CmdPlugin.S3CmdProvision icgcGetProvision;
    final String resourcesDirectory = new File("src/test/resources").getAbsolutePath();
    @Before
    public void before() throws Exception {
        icgcGetProvision = new S3CmdPlugin.S3CmdProvision();
        setConfiguration();
    }

    private void setConfiguration() throws Exception {
        HashMap hm = new HashMap();
        hm.put("client", "/home/travis/.local/bin/s3cmd");
//        hm.put("client", "/usr/local/bin/s3cmd");
        hm.put("config-file-location", resourcesDirectory + "/.s3cfg");
        hm.put("verbosity", "Minimal");
        icgcGetProvision.setConfiguration(hm);
    }

    private void download(String source, String destination) {
        String sourcePath = source;
        File f = new File(destination);
        assertFalse(f.getAbsolutePath() + " already exists.",f.exists());
        Path destinationPath = Paths.get(f.getAbsolutePath());
        assertTrue(icgcGetProvision.downloadFrom(sourcePath, destinationPath));
        assertTrue(f.getAbsolutePath() + " not downloaded." , f.exists());
    }

    /**
     * This tests if a file can be uploaded to another file and then downloaded as a different file
     */
    @Test
    public void uploadFileToFile() {
        String destPath = "s3cmd://test-bucket1/file2.txt";
        Path sourceFile = Paths.get(resourcesDirectory + "/inputFilesDirectory/file.txt");
        icgcGetProvision.uploadTo(destPath, sourceFile, null);
        String source = "s3cmd://test-bucket1/file2.txt";
        String destination = resourcesDirectory +"/outputFilesDirectory1/file3.txt";
        download(source, destination);
    }

    @Test
    public void uploadFileToDirectory() {
        String destPath = "s3cmd://test-bucket2/";
        Path sourceFile = Paths.get(resourcesDirectory + "/inputFilesDirectory/file.txt");
        icgcGetProvision.uploadTo(destPath, sourceFile, null);
        String source = "s3cmd://test-bucket2/file.txt";
        String destination = resourcesDirectory +"/outputFilesDirectory2/file2.txt";
        download(source, destination);
    }

    @Test
    public void uploadDirectoryToDirectory() {
        String destPath = "s3cmd://test-bucket3/";
        Path sourceFile = Paths.get(resourcesDirectory + "/inputFilesDirectory");
        icgcGetProvision.uploadTo(destPath, sourceFile, null);
        String source = "s3cmd://test-bucket3/inputFilesDirectory/file.txt";
        String destination = resourcesDirectory +"/outputFilesDirectory3/file.txt";
        download(source, destination);
        source = "s3cmd://test-bucket3/inputFilesDirectory/file2.txt";
        destination = resourcesDirectory +"/outputFilesDirectory3/file2.txt";
        download(source, destination);
    }
}
