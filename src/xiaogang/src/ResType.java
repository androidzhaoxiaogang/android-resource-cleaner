package xiaogang.src;

import java.io.File;

public abstract class ResType {
    private final String mType;

    public ResType(final String type) {
        super();
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public abstract boolean doesFileDeclareResource(File parent, String fileName, String fileContents, String resourceName);

    public boolean doesFileUseResource(final File parent, final String fileName, final String fileContents, final String resourceName) {
        return false;
    }
}
