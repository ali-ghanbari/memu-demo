package edu.iastate.memo.maven;

import edu.iastate.memo.MemoizerEntryPoint;
import edu.iastate.memo.commons.functional.PredicateFactory;
import edu.iastate.memo.commons.misc.NameUtils;
import edu.iastate.memo.constants.Params;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pitest.classpath.ClassPath;
import org.pitest.functional.predicate.Predicate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maven plugin Mojo for the memoization system
 * The user is also able to configure the plugin using properties
 * mentioned in <code>{@link Params}</code>.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
abstract class AbstractMemoMojo extends AbstractMojo {
    private static final String[] RESOURCES = {
            "bddbddb-full.jar",
            "accesses.dlog",
            "implicates.dlog",
            "dominator.dlog"
    };

    private File compatibleJREHome;

    private Predicate<String> appClassFilter;

    private Predicate<String> testClassFilter;

    private Predicate<String> failingTestFilter;

    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "plugin.artifactMap", readonly = true, required = true)
    private Map<String, Artifact> pluginArtifactMap;

    // -----------------------
    // ---- plugin params ----
    // -----------------------

    /**
     * Threshold in millisecond. All methods costlier than <code>threshold</code>
     * shall be considered memoized.
     */
    @Parameter(property = "threshold", defaultValue = "10")
    protected double threshold;

    /**
     * Maximum number of methods satisfying threshold to be memoized
     * More precisely, given the set M of all covered methods,
     * top limit elements of the set {m in M | time(m) <= threshold}
     * will be selected for memoization.
     *
     * Enter 0 (or any integer below 0) to disable limiting
     */
    @Parameter(property = "limit", defaultValue = "15")
    protected int limit;

    // test case names should be of the form testClassName:testMethodName
    @Parameter(property = "failingTests")
    protected Set<String> failingTests;

    @Parameter(property = "targetClasses")
    protected Set<String> targetClasses;

    @Parameter(property = "excludedClasses")
    protected Set<String> excludedClasses;

    /**
     * Using this parameter one can narrow down the space of selected test cases
     * during profiling and patch validation.
     */
    @Parameter(property = "targetTests")
    protected Set<String> targetTests;

    @Parameter(property = "excludedTests")
    protected Set<String> excludedTests;

    @Parameter(property = "forceProfiling", defaultValue = "false")
    protected boolean forceProfiling;

    @Parameter(property = "measureTestingTime", defaultValue = "false")
    protected boolean measureTestingTime;

    @Parameter(property = "skipProvisionalMemoization", defaultValue = "false")
    protected boolean skipProvisionalMemoization;

    @Parameter(property = "specialMethods")
    protected Set<String> specialMethods;

    @Parameter(property = "childProcessArguments")
    protected List<String> childProcessArguments;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkAndSanitizeParameters();
        setupDatalog();

        final ClassPath classPath = createClassPath();

        final MemoizerEntryPoint entryPoint = new MemoizerEntryPoint(classPath,
                this.compatibleJREHome,
                this.appClassFilter,
                this.testClassFilter,
                this.failingTestFilter,
                this.threshold,
                this.limit,
                this.forceProfiling,
                this.measureTestingTime,
                this.skipProvisionalMemoization,
                this.specialMethods,
                this.childProcessArguments);
        entryPoint.start();
    }

    private void checkAndSanitizeParameters() throws MojoFailureException {
        final String jreHome = System.getProperty("java.home");
        if (jreHome == null) {
            throw new MojoFailureException("JAVA_HOME is not set");
        }
        this.compatibleJREHome = new File(jreHome);
        if (!this.compatibleJREHome.isDirectory()) {
            throw new MojoFailureException("Invalid JAVA_HOME");
        }

        final String groupId = this.project.getGroupId();

        if (this.excludedClasses == null) {
            this.excludedClasses = Collections.emptySet();
        }
        final Predicate<String> excludedClassFilter = PredicateFactory.orGlobs(this.excludedClasses);

        if (this.targetClasses == null) {
            this.targetClasses = new HashSet<>();
        }
        if (this.targetClasses.isEmpty()) {
            this.targetClasses.add(groupId + ".*");
        }
        this.appClassFilter = PredicateFactory.orGlobs(this.targetClasses);
        this.appClassFilter = PredicateFactory.and(this.appClassFilter, PredicateFactory.not(excludedClassFilter));

        if (this.excludedTests == null) {
            this.excludedTests = Collections.emptySet();
        }
        final Predicate<String> excludedTestFilter = PredicateFactory.orGlobs(this.excludedTests);

        if (this.targetTests == null) {
            this.targetTests = new HashSet<>();
        }
        if (this.targetTests.isEmpty()) {
            this.targetTests.add(String.format("%s*Test", groupId));
            this.targetTests.add(String.format("%s*Tests", groupId));
        }
        this.testClassFilter = PredicateFactory.orGlobs(this.targetTests);
        this.testClassFilter = PredicateFactory.and(this.testClassFilter, PredicateFactory.not(excludedTestFilter));

        if (this.threshold < 0) {
            throw new MojoFailureException("Invalid threshold value " + this.threshold);
        }

        if (this.limit < 0) {
            getLog().warn("Limiting is disabled");
            getLog().warn("Any method taking " + this.threshold + " ms or less will be memoized");
            this.limit = Integer.MAX_VALUE;
        }

        if (this.failingTests == null) {
            this.failingTests = Collections.emptySet();
        }
        final Set<String> failingTests = new HashSet<>();
        for (String testName : this.failingTests) {
            final int indexOfLP = testName.lastIndexOf('(');
            if (indexOfLP >= 0) {
                testName = testName.substring(0, indexOfLP);
            }
            failingTests.add(NameUtils.sanitizeTestName(testName.trim()) + "()");
        }
        this.failingTests = failingTests;
        this.failingTestFilter = PredicateFactory.orGlobs(failingTests);

        if (this.specialMethods == null) {
            this.specialMethods = Collections.emptySet();
        }

        if (this.childProcessArguments == null) {
            this.childProcessArguments = Collections.singletonList("-Xmx64g");
        }
    }

    private void setupDatalog() {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        for (final String fileName : RESOURCES) {
            final File outFile = new File(fileName);
            outFile.deleteOnExit();
            try (final InputStream is = classloader.getResourceAsStream(fileName);
                 final OutputStream os = new FileOutputStream(outFile)) {
                if (is == null) {
                    throw new MojoFailureException(String.format("%s does not exist", fileName));
                }
                IOUtils.copy(is, os);
            } catch (Exception e) {
                getLog().warn(e);
            }
        }
    }

    private List<File> getProjectClassPath() {
        final List<File> classPath = new ArrayList<>();
        try {
            for (final Object cpElement : this.project.getTestClasspathElements()) {
                classPath.add(new File((String) cpElement));
            }
        } catch (DependencyResolutionRequiredException e) {
            getLog().warn(e);
        }
        return classPath;
    }

    private List<File> getPluginClassPath() {
        final List<File> classPath = new ArrayList<>();
        for (final Artifact dependency : this.pluginArtifactMap.values()) {
            if (isRelevantDep(dependency)) {
                classPath.add(dependency.getFile());
            }
        }
        return classPath;
    }

    private static boolean isRelevantDep(final Artifact dependency) {
        return dependency.getGroupId().equals("edu.iastate")
                && dependency.getArtifactId().equals("memoizer-maven-plugin");
    }

    private ClassPath createClassPath() {
        final List<File> classPathElements = new ArrayList<>();
        classPathElements.addAll(getProjectClassPath());
        classPathElements.addAll(getPluginClassPath());
        return new ClassPath(classPathElements);
    }
}