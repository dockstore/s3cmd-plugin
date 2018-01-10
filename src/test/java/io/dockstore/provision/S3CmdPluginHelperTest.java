package io.dockstore.provision;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author gluu
 * @since 10/01/18
 */
public class S3CmdPluginHelperTest {
    @Test
    public void getChunkSize() throws Exception {
        String flag = S3CmdPluginHelper.getChunkSize(150000000001L);
        assertEquals(" --multipart-chunk-size-mb=16", flag);
        flag = S3CmdPluginHelper.getChunkSize(150000000000L);
        assertEquals("", flag);
        flag = S3CmdPluginHelper.getChunkSize(149999999999L);
        assertEquals("", flag);
    }

    @Test
    public void nextLinesRequireCarriageReturn() throws Exception {
        String line = "upload: '/home/gluu/testCLI/datastore/launcher-a8a39655-951a-4a3a-8df2-1ec8dade22e7/outputs/md5sum.txt' -> 's3://dockstore.temp/thing2.txt'  [1 of 1]";
        boolean b = S3CmdPluginHelper.nextLinesRequireCarriageReturn(line);
        assertTrue(S3CmdPluginHelper.nextLinesRequireCarriageReturn(line));
        line = "download: 's3://dockstore.temp/thing2.txt' -> '/home/gluu/testCLI/datastore/launcher-a8a39655-951a-4a3a-8df2-1ec8dade22e7/inputs/9b509df8-9ece-4e70-91be-ad7e7ba41564/thing2.txt'  [1 of 1]";
        assertTrue(S3CmdPluginHelper.nextLinesRequireCarriageReturn(line));
        line = "33 of 33   100% in    0s    95.46 B/s  done";
        assertFalse(S3CmdPluginHelper.nextLinesRequireCarriageReturn(line));
    }

}
