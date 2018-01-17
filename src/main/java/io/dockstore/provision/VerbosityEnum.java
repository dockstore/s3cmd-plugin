package io.dockstore.provision;

/**
 * @author gluu
 * @since 17/01/18
 */
public enum VerbosityEnum
{
    QUIET(1), MINIMAL(2), NORMAL(3), DETAILED(4), DIAGNOSTIC(5);

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
