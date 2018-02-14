package io.dockstore.provision;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author gluu
 * @since 14/02/18
 */
public class VerbosityEnumTest {
    /**
     * This tests if the enum can be converted to the correct integer
     */
    @Test
    public void getLevel() throws Exception {
        Assert.assertEquals(1 ,VerbosityEnum.QUIET.getLevel());
        Assert.assertEquals(2 ,VerbosityEnum.MINIMAL.getLevel());
        Assert.assertEquals(3 ,VerbosityEnum.NORMAL.getLevel());
        Assert.assertEquals(4 ,VerbosityEnum.DETAILED.getLevel());
        Assert.assertEquals(5 ,VerbosityEnum.DIAGNOSTIC.getLevel());
    }

}
