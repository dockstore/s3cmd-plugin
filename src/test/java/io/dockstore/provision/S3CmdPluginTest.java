package io.dockstore.provision;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author gluu
 * @since 07/03/17
 */
public class S3CmdPluginTest {
    final String resourcesDirectory = new File("src/test/resources").getAbsolutePath();
    S3CmdPlugin.S3CmdProvision icgcGetProvision;

    @Before
    public void before() throws Exception {
        icgcGetProvision = new S3CmdPlugin.S3CmdProvision();
        setConfiguration();
    }

    private void setConfiguration() throws Exception {
        HashMap hm = new HashMap();
        List<String> s3cmdLocations = Arrays.asList("/usr/local/bin/s3cmd", "/home/travis/.local/bin/s3cmd");
        Optional<String> first = s3cmdLocations.stream().filter(s3cmdLocation -> {
            File file = new File(s3cmdLocation);
            return file.exists();
        }).findFirst();
        if (first.isPresent()) {
            hm.put("client", first.get());
        } else {
            throw new RuntimeException("No s3cmd installed");
        }
        hm.put("config-file-location", resourcesDirectory + "/.s3cfg");
        hm.put("verbosity", "Minimal");
        icgcGetProvision.setConfiguration(hm);
    }

    private void download(String source, String destination) {
        String sourcePath = source;
        File f = new File(destination);
        assertFalse(f.getAbsolutePath() + " already exists.", f.exists());
        Path destinationPath = Paths.get(f.getAbsolutePath());
        assertTrue(icgcGetProvision.downloadFrom(sourcePath, destinationPath));
        assertTrue(f.getAbsolutePath() + " not downloaded.", f.exists());
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
        String destination = resourcesDirectory + "/outputFilesDirectory1/file3.txt";
        download(source, destination);
    }

    /**
     * This tests if a file can be uploaded to a directory then downloaded as a different file
     */
    @Test
    public void uploadFileToDirectory() {
        String destPath = "s3cmd://test-bucket2/";
        Path sourceFile = Paths.get(resourcesDirectory + "/inputFilesDirectory/file.txt");
        icgcGetProvision.uploadTo(destPath, sourceFile, null);
        String source = "s3cmd://test-bucket2/file.txt";
        String destination = resourcesDirectory + "/outputFilesDirectory2/file2.txt";
        download(source, destination);
    }

    /**
     * This tests if a directory can be uploaded to another directory and then all of its files could be downloaded
     */
    @Test
    public void uploadDirectoryToDirectory() {
        String destPath = "s3cmd://test-bucket3/";
        Path sourceFile = Paths.get(resourcesDirectory + "/inputFilesDirectory");
        icgcGetProvision.uploadTo(destPath, sourceFile, null);
        String source = "s3cmd://test-bucket3/inputFilesDirectory/file.txt";
        String destination = resourcesDirectory + "/outputFilesDirectory3/file.txt";
        download(source, destination);
        source = "s3cmd://test-bucket3/inputFilesDirectory/file2.txt";
        destination = resourcesDirectory + "/outputFilesDirectory3/file2.txt";
        download(source, destination);
    }
}
