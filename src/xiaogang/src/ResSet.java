package xiaogang.src;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ResSet implements Comparable<ResSet> {
    private final String mType;
    private final String mName;

    private final SortedSet<String> mDeclaredPaths = new TreeSet<String>();
    private final Set<String> mConfigurations = new HashSet<String>();

    private static final String sStringFormat = "%s %s";
    private static final String sPathFormat = "%s";

    public String getPath() {
        final StringBuilder stringBuilder = new StringBuilder();

        for (final String path : mDeclaredPaths) {
            stringBuilder.append(String.format(sPathFormat, path));
        }

        return stringBuilder.toString();
    }

    public ResSet(final String type, final String name) {
        super();
        mType = type;
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public void addDeclaredPath(final String path) {
        mDeclaredPaths.add(path);
    }

    public boolean hasNoDeclaredPaths() {
        return mDeclaredPaths.isEmpty();
    }

    public void addConfiguration(final String configuration) {
        mConfigurations.add(configuration);
    }

    public Set<String> getConfigurations() {
        return mConfigurations;
    }

    @Override
    public int compareTo(final ResSet another) {
        final int typeComparison = mType.compareTo(another.getType());

        if (typeComparison != 0) {
            return typeComparison;
        }

        return mName.compareTo(another.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof ResSet)) {
            return false;
        }

        final ResSet resource = (ResSet) o;

        return mType.equals(resource.getType()) && mName.equals(resource.getName());
    }

    @Override
    public int hashCode() {
        return (mType + '/' + mName).hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder(String.format(sStringFormat, mType, mName));

        for (final String path : mDeclaredPaths) {
            stringBuilder.append(" ");
            stringBuilder.append(String.format(sPathFormat, path));
        }

        return stringBuilder.toString();
    }
}
