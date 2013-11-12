
package xiaogang.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ResProbe {
    private volatile boolean isCanceled = false;
    private String mPackageName;
    private ARC mCallback;

    private File mBaseDirectory;
    private File mSrcDirectory;
    private File mResDirectory;
    private File mGenDirectory;
    private File mManifestFile;
    private File mRJavaFile;

    private List<ResSet> mResList = new ArrayList<ResSet>();
    private final Set<ResSet> mResources = new HashSet<ResSet>();
    private final Set<ResSet> mUsedResources = new HashSet<ResSet>();

    private static final Pattern sResourceTypePattern = Pattern
            .compile("^\\s*public static final class (\\w+)\\s*\\{$");
    private static final Pattern sResourceNamePattern = Pattern
            .compile("^\\s*public static( final)? int(\\[\\])? (\\w+)\\s*=\\s*(\\{|(0x)?[0-9A-Fa-f]+;)\\s*$");
    private static final FileType sJavaFileType = new FileType("java", "R." + FileType.USAGE_TYPE
            + "." + FileType.USAGE_NAME + "[^\\w_]");
    private static final FileType sXmlFileType = new FileType("xml", "[\" >]@"
            + FileType.USAGE_TYPE + "/" + FileType.USAGE_NAME + "[\" <]");

    private static final Map<String, ResType> sResourceTypes = new HashMap<String, ResType>();

    static {
        // anim
        sResourceTypes.put("anim", new ResType("anim") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals(getType())) {
                    return false;
                }

                final String name = fileName.split("\\.")[0];
                final Pattern pattern = Pattern.compile("^" + resourceName + "$");
                return pattern.matcher(name).find();
            }
        });

        // array
        sResourceTypes.put("array", new ResType("array") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<([a-z]+\\-)?array.*?name\\s*=\\s*\""
                        + resourceName + "\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // attr
        sResourceTypes.put("attr", new ResType("attr") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<attr.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }

            @Override
            public boolean doesFileUseResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (parent != null) {
                    if (!parent.isDirectory()) {
                        return false;
                    }

                    final String directoryType = parent.getName().split("-")[0];
                    if (!directoryType.equals("layout") && !directoryType.equals("values")) {
                        return false;
                    }
                }

                final Pattern pattern = Pattern.compile("<.+?:" + resourceName
                        + "\\s*=\\s*\".*?\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                final Pattern itemPattern = Pattern.compile("<item.+?name\\s*=\\s*\""
                        + resourceName + "\".*?>");
                final Matcher itemMatcher = itemPattern.matcher(fileContents);

                if (itemMatcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // bool
        sResourceTypes.put("bool", new ResType("bool") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<bool.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // color
        sResourceTypes.put("color", new ResType("color") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<color.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // dimen
        sResourceTypes.put("dimen", new ResType("dimen") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<dimen.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // drawable
        sResourceTypes.put("drawable", new ResType("drawable") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (directoryType.equals(getType())) {

                    final String name = fileName.split("\\.")[0];
                    final Pattern pattern = Pattern.compile("^" + resourceName + "$");
                    return pattern.matcher(name).find();
                }

                if (directoryType.equals("values")) {
                    final Pattern pattern = Pattern.compile("<drawable.*?name\\s*=\\s*\""
                            + resourceName + "\".*?/?>");
                    final Matcher matcher = pattern.matcher(fileContents);
                    if (matcher.find()) {
                        return true;
                    }
                }

                return false;
            }
        });

        // id
        sResourceTypes.put("id", new ResType("id") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values") && !directoryType.equals("layout")) {
                    return false;
                }

                final Pattern valuesPattern0 = Pattern
                        .compile("<item.*?type\\s*=\\s*\"id\".*?name\\s*=\\s*\"" + resourceName
                                + "\".*?/?>");
                final Pattern valuesPattern1 = Pattern.compile("<item.*?name\\s*=\\s*\""
                        + resourceName + "\".*?type\\s*=\\s*\"id\".*?/?>");
                final Pattern layoutPattern = Pattern.compile(":id\\s*=\\s*\"@\\+id/"
                        + resourceName + "\"");
                Matcher matcher = valuesPattern0.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                matcher = valuesPattern1.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                matcher = layoutPattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // integer
        sResourceTypes.put("integer", new ResType("integer") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<integer.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");
                final Matcher matcher = pattern.matcher(fileContents);
                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // layout
        sResourceTypes.put("layout", new ResType("layout") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals(getType())) {
                    return false;
                }

                final String name = fileName.split("\\.")[0];

                final Pattern pattern = Pattern.compile("^" + resourceName + "$");

                return pattern.matcher(name).find();
            }
        });

        // menu
        sResourceTypes.put("menu", new ResType("menu") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals(getType())) {
                    return false;
                }

                final String name = fileName.split("\\.")[0];
                final Pattern pattern = Pattern.compile("^" + resourceName + "$");
                return pattern.matcher(name).find();
            }
        });

        // plurals
        sResourceTypes.put("plurals", new ResType("plurals") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<plurals.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");
                final Matcher matcher = pattern.matcher(fileContents);
                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // raw
        sResourceTypes.put("raw", new ResType("raw") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals(getType())) {
                    return false;
                }

                final String name = fileName.split("\\.")[0];
                final Pattern pattern = Pattern.compile("^" + resourceName + "$");
                return pattern.matcher(name).find();
            }
        });

        // string
        sResourceTypes.put("string", new ResType("string") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<string.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");
                final Matcher matcher = pattern.matcher(fileContents);
                if (matcher.find()) {
                    return true;
                }

                return false;
            }
        });

        // style
        sResourceTypes.put("style", new ResType("style") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final Pattern pattern = Pattern.compile("<style.*?name\\s*=\\s*\"" + resourceName
                        + "\".*?/?>");
                final Matcher matcher = pattern.matcher(fileContents);
                if (matcher.find()) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean doesFileUseResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (parent != null) {
                    if (!parent.isDirectory()) {
                        return false;
                    }

                    final String directoryType = parent.getName().split("-")[0];
                    if (!directoryType.equals("values")) {
                        return false;
                    }
                }

                // (name="Parent.Child")
                final Pattern pattern = Pattern.compile("<style.*?name\\s*=\\s*\"" + resourceName
                        + "\\.\\w+\".*?/?>");

                final Matcher matcher = pattern.matcher(fileContents);

                if (matcher.find()) {
                    return true;
                }

                // (parent="Parent")
                final Pattern pattern1 = Pattern.compile("<style.*?parent\\s*=\\s*\""
                        + resourceName + "\".*?/?>");
                final Matcher matcher1 = pattern1.matcher(fileContents);
                if (matcher1.find()) {
                    return true;
                }
                return false;
            }
        });

        // styleable
        sResourceTypes.put("styleable", new ResType("styleable") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals("values")) {
                    return false;
                }

                final String[] styleableAttr = resourceName.split("\\[_\\\\.\\]");

                if (styleableAttr.length == 1) {

                    final Pattern pattern = Pattern.compile("<declare-styleable.*?name\\s*=\\s*\""
                            + styleableAttr[0] + "\".*?/?>");
                    final Matcher matcher = pattern.matcher(fileContents);

                    if (matcher.find()) {
                        return true;
                    }

                    return false;
                }

                final Pattern blockPattern = Pattern.compile("<declare-styleable.*?name\\s*=\\s*\""
                        + styleableAttr[0] + "\".*?>(.*?)</declare-styleable\\s*>");
                final Matcher blockMatcher = blockPattern.matcher(fileContents);

                if (blockMatcher.find()) {
                    final String styleableAttributes = blockMatcher.group(1);

                    final Pattern attributePattern = Pattern.compile("<attr.*?name\\s*=\\s*\""
                            + styleableAttr[1] + "\".*?/?>");
                    final Matcher attributeMatcher = attributePattern.matcher(styleableAttributes);
                    if (attributeMatcher.find()) {
                        return true;
                    }
                    return false;
                }

                return false;
            }
        });

        // xml
        sResourceTypes.put("xml", new ResType("xml") {
            @Override
            public boolean doesFileDeclareResource(final File parent, final String fileName,
                    final String fileContents, final String resourceName) {
                if (!parent.isDirectory()) {
                    return false;
                }

                final String directoryType = parent.getName().split("-")[0];
                if (!directoryType.equals(getType())) {
                    return false;
                }

                final String name = fileName.split("\\.")[0];
                final Pattern pattern = Pattern.compile("^" + resourceName + "$");
                return pattern.matcher(name).find();
            }
        });
    }

    public ResProbe() {
        super();
        final String baseDirectory = System.getProperty("user.dir");
        mBaseDirectory = new File(baseDirectory);
    }

    protected ResProbe(final String baseDirectory) {
        super();
        mBaseDirectory = new File(baseDirectory);
    }

    public void delete(int[] list) {
        LinkedList<ResSet> remove = new LinkedList<ResSet>();
        for (int i : list) {
            ResSet result = mResList.get(i);
            final String type = result.getType();
            System.out.println("type: "+type);
            if (type.equals("anim") || type.equals("drawable") || type.equals("layout")) {
                final String path = result.getPath();
                System.out.println("path: "+path);
                if (!isEmpty(path)) {
                    File file = new File(path);
                    file.delete();
                    remove.add(result);
                }
            }
        }
        mResList.removeAll(remove);
    }

    public String[] getResult() {
        String[] result = new String[mResList.size()];
        for (int i = 0; i < mResList.size(); i++) {
            result[i] = mResList.get(i).toString();
        }
        return result;
    }

    public void deleteAll() {
        LinkedList<ResSet> remove = new LinkedList<ResSet>();
        for (ResSet resource : mResList) {
            final String type = resource.getType();
            if (type.equals("anim") || type.equals("drawable") || type.equals("layout") ||type.equals("menu")) {
                final String path = resource.getPath();
                if (!isEmpty(path)) {
                    File file = new File(path);
                    file.delete();
                    remove.add(resource);
                }
            }
        }
        mResList.removeAll(remove);
    }

    private boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    public void showResult() {
        if (mCallback != null) {
            mCallback.setResult(mResList);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (mResList != null) {
            mResList.clear();
        }
        super.finalize();
    }

    public void run(ARC callback) {

        isCanceled = false;

        if (callback != null) {
            this.mCallback = callback;
        }

        System.out.println("Running in: " + mBaseDirectory.getAbsolutePath());

        findPaths();

        if (mSrcDirectory == null || mResDirectory == null || mManifestFile == null) {
            System.err.println("The current directory is not a valid Android project root.");
            return;
        }

        mPackageName = findPackageName(mManifestFile);

        if (mPackageName == null || mPackageName.trim().length() == 0) {
            return;
        }

        if (mGenDirectory == null) {
            System.err.println("You must first build your project to generate R.java");
            return;
        }

        mRJavaFile = findRJavaFile(mGenDirectory, mPackageName);

        if (mRJavaFile == null) {
            System.err.println("You must first build your project to generate R.java");
            return;
        }

        mResources.clear();

        try {
            mResources.addAll(getResourceList(mRJavaFile));//²éÕÒRÎÄ¼þ
        } catch (final IOException e) {
            System.err.println("The R.java found could not be opened.");
            e.printStackTrace();
        }

        if (isCanceled) {
            return;
        }

        System.out.println(mResources.size() + " resources found");
        System.out.println();

        mUsedResources.clear();

        searchFiles(null, mSrcDirectory, sJavaFileType);
        if (isCanceled) {
            return;
        }
        searchFiles(null, mResDirectory, sXmlFileType);
        if (isCanceled) {
            return;
        }
        searchFiles(null, mManifestFile, sXmlFileType);
        if (isCanceled) {
            return;
        }

        /*
         * Because attr and styleable are so closely linked, we need to do some
         * matching now to ensure we don't say an attr is unused if its
         * corresponding styleable is used.
         */
        final Set<ResSet> extraUsedResources = new HashSet<ResSet>();

        for (final ResSet resource : mResources) {
            if (resource.getType().equals("styleable")) {
                final String[] styleableAttr = resource.getName().split("_");

                if (styleableAttr.length > 1) {
                    final String attrName = styleableAttr[1];

                    final ResSet attrResourceTest = new ResSet("attr", attrName);

                    if (mUsedResources.contains(attrResourceTest)) {
                        extraUsedResources.add(resource);
                    }
                }
            } else if (resource.getType().equals("attr")) {
                for (final ResSet usedResource : mUsedResources) {
                    if (usedResource.getType().equals("styleable")) {
                        final String[] styleableAttr = usedResource.getName().split("_");

                        if (styleableAttr.length > 1 && styleableAttr[1].equals(resource.getName())) {
                            extraUsedResources.add(resource);
                        }
                    }
                }
            }
        }

        for (final ResSet resource : extraUsedResources) {
            mResources.remove(resource);
            mUsedResources.add(resource);
        }

        final SortedMap<String, SortedMap<String, ResSet>> unusedResources = new TreeMap<String, SortedMap<String, ResSet>>();

        for (final ResSet resource : mResources) {
            final String type = resource.getType();
            SortedMap<String, ResSet> typeMap = unusedResources.get(type);

            if (typeMap == null) {
                typeMap = new TreeMap<String, ResSet>();
                unusedResources.put(type, typeMap);
            }

            typeMap.put(resource.getName(), resource);
        }

        final Map<String, ResType> unusedResourceTypes = new HashMap<String, ResType>(
                unusedResources.size());

        for (final String type : unusedResources.keySet()) {
            final ResType resourceType = sResourceTypes.get(type);
            if (resourceType != null) {
                unusedResourceTypes.put(type, resourceType);
            }
        }

        findDeclaredPaths(null, mResDirectory, unusedResourceTypes, unusedResources);

        /*
         * Find the paths where the used resources are declared.
         */
        final SortedMap<String, SortedMap<String, ResSet>> usedResources = new TreeMap<String, SortedMap<String, ResSet>>();

        for (final ResSet resource : mUsedResources) {
            final String type = resource.getType();
            SortedMap<String, ResSet> typeMap = usedResources.get(type);

            if (typeMap == null) {
                typeMap = new TreeMap<String, ResSet>();
                usedResources.put(type, typeMap);
            }

            typeMap.put(resource.getName(), resource);
        }

        if (isCanceled) {
            return;
        }

        final Map<String, ResType> usedResourceTypes = new HashMap<String, ResType>(
                usedResources.size());

        for (final String type : usedResources.keySet()) {
            final ResType resourceType = sResourceTypes.get(type);
            if (resourceType != null) {
                usedResourceTypes.put(type, resourceType);
            }
        }

        if (isCanceled) {
            return;
        }

        findDeclaredPaths(null, mResDirectory, usedResourceTypes, usedResources);

        final Set<ResSet> libraryProjectResources = getLibraryProjectResources();

        for (final ResSet libraryResource : libraryProjectResources) {
            final SortedMap<String, ResSet> typedResources = unusedResources.get(libraryResource
                    .getType());

            if (typedResources != null) {
                final ResSet appResource = typedResources.get(libraryResource.getName());

                if (appResource != null && appResource.hasNoDeclaredPaths()) {
                    typedResources.remove(libraryResource.getName());
                    mResources.remove(appResource);
                }
            }
        }

        if (isCanceled) {
            return;
        }

        final int unusedResourceCount = mResources.size();

        if (unusedResourceCount > 0) {
            System.out.println(unusedResourceCount + " unused resources were found:");

            final SortedSet<ResSet> sortedResources = new TreeSet<ResSet>(mResources);

            for (final ResSet resource : sortedResources) {
                System.out.println(resource);
            }
            mResList.addAll(sortedResources);
        }

        showResult();
    }

    private void findPaths() {
        final File[] children = mBaseDirectory.listFiles();

        if (children == null) {
            return;
        }

        for (final File file : children) {
            if (file.isDirectory()) {
                if (file.getName().equals("src")) {
                    mSrcDirectory = file;
                } else if (file.getName().equals("res")) {
                    mResDirectory = file;
                } else if (file.getName().equals("gen")) {
                    mGenDirectory = file;
                }
            } else if (file.getName().equals("AndroidManifest.xml")) {
                mManifestFile = file;
            }
        }
    }

    private static String findPackageName(final File androidManifestFile) {
        String manifest = "";

        try {
            manifest = FileUtils.getFileContents(androidManifestFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Pattern pattern = Pattern
                .compile("<manifest\\s+.*?package\\s*=\\s*\"([A-Za-z0-9_\\.]+)\".*?>");
        final Matcher matcher = pattern.matcher(manifest);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static File findRJavaFile(final File baseDirectory, final String packageName) {
        final File rJava = new File(baseDirectory, packageName.replace('.', '/') + "/R.java");

        if (rJava.exists()) {
            return rJava;
        }

        return null;
    }

    public void setbCancel(boolean bCancel) {
        this.isCanceled = bCancel;
    }

    /**
     * Removes all resources declared in library projects.
     */
    private Set<ResSet> getLibraryProjectResources() {
        final Set<ResSet> resources = new HashSet<ResSet>();

        final File projectPropertiesFile = new File(mBaseDirectory, "project.properties");

        if (!projectPropertiesFile.exists()) {
            return resources;
        }

        List<String> fileLines = new ArrayList<String>();
        try {
            fileLines = FileUtils.getFileLines(projectPropertiesFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Pattern libraryProjectPattern = Pattern.compile(
                "^android\\.library\\.reference\\.\\d+=(.*)$", Pattern.CASE_INSENSITIVE);

        final List<String> libraryProjectPaths = new ArrayList<String>();

        for (final String line : fileLines) {
            final Matcher libraryProjectMatcher = libraryProjectPattern.matcher(line);

            if (libraryProjectMatcher.find()) {
                libraryProjectPaths.add(libraryProjectMatcher.group(1));
            }
        }

        for (final String libraryProjectPath : libraryProjectPaths) {
            final File libraryProjectDirectory = new File(mBaseDirectory, libraryProjectPath);

            if (libraryProjectDirectory.exists() && libraryProjectDirectory.isDirectory()) {
                final String libraryProjectPackageName = findPackageName(new File(
                        libraryProjectDirectory, "AndroidManifest.xml"));
                final File libraryProjectRJavaFile = findRJavaFile(new File(
                        libraryProjectDirectory, "gen"), libraryProjectPackageName);

                if (libraryProjectRJavaFile != null) {
                    try {
                        resources.addAll(getResourceList(libraryProjectRJavaFile));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return resources;
    }

    private static Set<ResSet> getResourceList(final File rJavaFile) throws IOException {
        final InputStream inputStream = new FileInputStream(rJavaFile);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        boolean done = false;

        final Set<ResSet> resources = new HashSet<ResSet>();

        String type = "";

        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                final Matcher typeMatcher = sResourceTypePattern.matcher(line);
                final Matcher nameMatcher = sResourceNamePattern.matcher(line);

                if (nameMatcher.find()) {
                    resources.add(new ResSet(type, nameMatcher.group(3)));
                } else if (typeMatcher.find()) {
                    type = typeMatcher.group(1);
                }
            }
        }

        reader.close();
        inputStream.close();

        return resources;
    }

    private void searchFiles(final File parent, final File file, final FileType fileType) {
        if (isCanceled) {
            return;
        }
        if (file.isDirectory()) {
            for (final File child : file.listFiles()) {
                if (isCanceled) {
                    return;
                }
                searchFiles(file, child, fileType);
            }
        } else if (file.getName().endsWith(fileType.getExtension())) {
            try {
                if (isCanceled) {
                    return;
                }
                searchFile(parent, file, fileType);
                if (mCallback != null) {
                    mCallback.setProgress(file);
                }
            } catch (final IOException e) {
                System.err.println("There was a problem reading " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    private void searchFile(final File parent, final File file, final FileType fileType)
            throws IOException {
        final Set<ResSet> foundResources = new HashSet<ResSet>();

        final String fileContents = FileUtils.getFileContents(file);

        for (final ResSet resource : mResources) {
            if (isCanceled) {
                return;
            }
            final Matcher matcher = fileType.getPattern(resource.getType(),
                    resource.getName().replace("_", "[_\\.]")).matcher(fileContents);

            if (matcher.find()) {
                foundResources.add(resource);
            } else {
                final ResType type = sResourceTypes.get(resource.getType());

                if (type != null
                        && type.doesFileUseResource(parent, file.getName(), fileContents, resource
                                .getName().replace("_", "[_\\.]"))) {
                    foundResources.add(resource);
                }
            }
        }

        for (final ResSet resource : foundResources) {
            if (isCanceled) {
                return;
            }
            mUsedResources.add(resource);
            mResources.remove(resource);
        }
    }

    private void findDeclaredPaths(final File parent, final File file,
            final Map<String, ResType> resourceTypes,
            final Map<String, SortedMap<String, ResSet>> resources) {
        if (file.isDirectory()) {
            for (final File child : file.listFiles()) {
                if (isCanceled) {
                    return;
                }
                if (!child.isHidden()) {
                    findDeclaredPaths(file, child, resourceTypes, resources);
                }
            }
        } else {
            if (!file.isHidden()) {
                final String fileName = file.getName();

                String fileContents = "";
                try {
                    fileContents = FileUtils.getFileContents(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                for (final ResType resourceType : resourceTypes.values()) {
                    if (isCanceled) {
                        return;
                    }
                    final Map<String, ResSet> typeMap = resources.get(resourceType.getType());

                    if (typeMap != null) {
                        for (final ResSet resource : typeMap.values()) {
                            if (isCanceled) {
                                return;
                            }
                            if (resourceType.doesFileDeclareResource(parent, fileName,
                                    fileContents, resource.getName().replace("_", "[_\\.]"))) {
                                resource.addDeclaredPath(file.getAbsolutePath());

                                final String configuration = parent.getName();
                                resource.addConfiguration(configuration);
                            }
                        }
                    }
                }
            }
        }
    }
}
