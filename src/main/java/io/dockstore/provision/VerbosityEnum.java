package io.dockstore.provision;

/**
 * @author gluu
 * @since 17/01/18
 */
public enum VerbosityEnum
{
    MINIMAL(0), NORMAL(1);

    private int level;

    VerbosityEnum(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return this.level;
    }
}
