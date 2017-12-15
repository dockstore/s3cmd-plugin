package io.dockstore.provision;

/**
 * @author gluu
 * @since 15/12/17
 */
public class S3CmdPluginHelper {
    private static final long DEFAULT_CHUNK_SIZE = 15;
    private static final long MAX_PARTS = 10000;
    /**
     * Calculates the chunk size for uploads and the flag to set it.
     * If the file is under 150 GB, the chunk size is left as the default (15 MB).
     * For files over 150 GB, the chunk size is kept as close to the default as possible with
     * the number of chunks at the max limit (10 000).  More chunks and chunk-size closer to default is safer.
     * Chunk size over 5 GB (passed the current limit) is automatically caught by s3cmd itself
     *
     * @param sizeInBytes Size of the file trying to upload in bytes
     * @return
     */
    public static String getChunkSize(long sizeInBytes) {
        String modifiedChunkSize = "";
        double sizeInMegabytes = (double)sizeInBytes/1000000;
        if (sizeInMegabytes > DEFAULT_CHUNK_SIZE*MAX_PARTS) {
            long newChunkSize = (long)Math.ceil(sizeInMegabytes/MAX_PARTS);
            modifiedChunkSize = " --multipart-chunk-size-mb=" + newChunkSize;
        }
        return modifiedChunkSize;
    }
}
