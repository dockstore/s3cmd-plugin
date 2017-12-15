package io.dockstore.provision;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author gluu
 * @since 07/03/17
 */
public class S3CmdPluginTest {

    @Test
    public void getChunkSizeTest() {
        String flag = S3CmdPluginHelper.getChunkSize(150000000001L);
        assertEquals(" --multipart-chunk-size-mb=16", flag);
        flag = S3CmdPluginHelper.getChunkSize(150000000000L);
        assertEquals("", flag);
        flag = S3CmdPluginHelper.getChunkSize(149999999999L);
        assertEquals("", flag);
    }
}
