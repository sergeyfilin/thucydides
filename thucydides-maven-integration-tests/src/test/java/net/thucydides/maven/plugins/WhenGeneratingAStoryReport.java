package net.thucydides.maven.plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WhenGeneratingAStoryReport {

    Verifier verifier;
    
    @After
    public void cleanupVerifier() throws VerificationException {
        if (verifier != null) {
            verifier.resetStreams();
        }
    }
    
    @Before
    public void generateTheHtmlReport() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/test-projects/simple-project");
        initVerifier(testDir);
        
        runTests();
    }

    private void runTests() {
        try {
            verifier.executeGoal("test");
        } catch (VerificationException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void the_aggregate_goal_should_produce_a_story_report() throws Exception {

        verifier.executeGoal("thucydides:aggregate");

        verifier.verifyErrorFreeLog();
        verifier.assertFilePresent("target/thucydides/stories.html");

        String report = getStringFrom(new File(verifier.getBasedir(), "target/thucydides/stories.html"));
        assertThat(report, is(not("")));
    }
    
    private Verifier initVerifier(File testDir) throws VerificationException, IOException {
        verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("net.thucydides.maven.plugins.samples", "simple-project", "1.0", "pom");
        verifier.deleteArtifact("net.thucydides.maven.plugins.samples", "simple-project", "1.0","jar");
        return verifier;
    }
    
    private String getStringFrom(File reportFile) throws IOException {
        return FileUtils.readFileToString(reportFile);
    }
}
