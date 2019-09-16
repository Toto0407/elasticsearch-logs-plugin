package io.jenkins.plugins.pipeline_elasticsearch_logs;

import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticSearchTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    List<Map<String, Object>> elasticSearchLoggedLines = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        ElasticSearchGlobalConfiguration.get().setElasticSearch(new ElasticSearchTestConfiguration(m -> elasticSearchLoggedLines.add(m)));
    }

    @After
    public void teardown() throws Exception {
        elasticSearchLoggedLines.clear();
    }

    @Test
    public void testSimpleJobOutput() throws Exception {
        FreeStyleProject project = jenkinsRule.createProject(FreeStyleProject.class);
        if (Functions.isWindows()) {
            project.getBuildersList().add(new BatchFile("echo 'yes'"));
            FreeStyleBuild build = jenkinsRule.buildAndAssertSuccess(project);

            assertLogLine(0, "buildStart");
            assertLogLine(1, "buildMessage", "Legacy code started this job.  No cause information is available");
            assertLogLine(4, "buildMessage", "");
            assertLogLine(6, "buildMessage", "'yes'");
            assertLogLine(7, "buildMessage", "");
            assertLogLine(9, "buildMessage", "Finished: SUCCESS");
            assertLogLine(10, "buildEnd");
            assertEquals(11, elasticSearchLoggedLines.size());
        }
        else {
            project.getBuildersList().add(new Shell("echo 'yes'"));
            FreeStyleBuild build = jenkinsRule.buildAndAssertSuccess(project);
            assertEquals(Arrays.asList("+ echo yes", "yes", "Finished: SUCCESS"), build.getLog(1000).subList(3, 6));

            assertLogLine(0, "buildStart");
            assertLogLine(1, "buildMessage", "Legacy code started this job.  No cause information is available");
            assertLogLine(4, "buildMessage", "+ echo yes");
            assertLogLine(5, "buildMessage", "yes");
            assertLogLine(6, "buildMessage", "Finished: SUCCESS");
            assertLogLine(7, "buildEnd");
            assertEquals(8, elasticSearchLoggedLines.size());
        }
    }

    private void assertLogLine(int lineIndex, String eventType, String message) {
        assertLogLine(lineIndex, "test0", "1", eventType, message);
    }

    @Test
    public void testSimplePipelineOutput() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  echo message: 'hello'" +
                "}", true));
        WorkflowRun build = jenkinsRule.buildAndAssertSuccess(project);

        // if elastic search output is enabled, nothing will be logged to build-log.
        assertEquals(Arrays.asList(), build.getLog(1000));

        assertEquals(22, elasticSearchLoggedLines.size());

        assertLogLine(0, "buildStart");
        assertLogLine(1, "buildMessage", "Started");
        assertLogLine(2, "buildMessage", "Running in Durability level: MAX_SURVIVABILITY");
        assertLogLine(3, "buildMessage", "[Pipeline] Start of Pipeline");
        assertLogLine(4, "flowGraph::flowStart");
        assertLogLine(5, "buildMessage", "[Pipeline] node");
        assertLogLine(6, "flowGraph::nodeStart");
        // skip 7
        assertLogLine(8, "buildMessage", "[Pipeline] {");
        assertLogLine(9, "flowGraph::nodeStart");
        assertLogLine(10, "buildMessage", "[Pipeline] echo");
        assertLogLine(11, "flowGraph::atomNodeStart");
        assertLogLine(12, "nodeMessage", "hello");
        assertLogLine(13, "buildMessage", "[Pipeline] }");
        assertLogLine(14, "flowGraph::atomNodeEnd");
        assertLogLine(15, "buildMessage", "[Pipeline] // node");
        assertLogLine(16, "flowGraph::nodeEnd");
        assertLogLine(17, "buildMessage", "[Pipeline] End of Pipeline");
        assertLogLine(18, "flowGraph::nodeEnd");
        assertLogLine(19, "flowGraph::flowEnd");
        assertLogLine(20, "buildMessage", "Finished: SUCCESS");
        assertLogLine(21, "buildEnd");
    }

    private void assertLogLine(int lineIndex, String eventType) {
        assertLogLine(lineIndex, eventType, null);
    }

    private void assertLogLine(int lineIndex, String project, String buildId, String eventType) {
        assertLogLine(lineIndex, project, buildId, eventType, null);
    }

    private void assertLogLine(int lineIndex, String project, String buildId, String eventType, String message) {
        Map<String, Object> line = elasticSearchLoggedLines.get(lineIndex);
        assertTrue(line.containsKey("timestampMillis"));
        assertTrue(line.containsKey("timestamp"));
        if (project != null && buildId != null) {
            Map<String, Object> runId = (Map<String, Object>) line.get("runId");
            assertEquals(project, runId.get("project"));
            assertEquals(buildId, runId.get("build"));
            assertEquals("testInstance", runId.get("instance"));
        }
        assertEquals(eventType, line.get("eventType"));
        if (message != null)
            assertEquals(message, line.get("message"));
    }

}