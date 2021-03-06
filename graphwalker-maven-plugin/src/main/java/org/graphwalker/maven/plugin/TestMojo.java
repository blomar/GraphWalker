/*
 * #%L
 * Maven GraphWalker Plugin
 * %%
 * Copyright (C) 2012 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.graphwalker.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.core.Machine;
import org.graphwalker.core.PathGenerator;
import org.graphwalker.core.SimpleMachine;
import org.graphwalker.core.StopCondition;
import org.graphwalker.core.common.ResourceUtils;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.ExecutionStatus;
import org.graphwalker.core.model.Element;
import org.graphwalker.maven.plugin.common.ReflectionUtils;
import org.graphwalker.maven.plugin.test.*;
import org.graphwalker.maven.plugin.test.Scanner;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE, lifecycle = "graphwalker")
public final class TestMojo extends AbstractTestMojo {

    private final Map<ExecutionContext, Object> implementations = new HashMap<ExecutionContext, Object>();
    private final List<Machine> machines = new ArrayList<Machine>();

    /**
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!getSkipTests()) {
            ClassLoader classLoader = switchClassLoader(createClassLoader());
            Properties properties = switchProperties(createProperties());
            displayHeader();
            Configuration configuration = createConfiguration();
            Scanner scanner = new Scanner();
            TestManager manager = new TestManager(configuration, scanner.scan(getTestClassesDirectory(), getClassesDirectory()));
            displayConfiguration(manager);
            executeTests(manager);
            displayResult(manager);
            switchProperties(properties);
            switchClassLoader(classLoader);
            reportResults(manager);
        }
    }

    private void displayHeader() {
        if (getLog().isInfoEnabled()) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info(" G r a p h W a l k e r                                                  ");
            getLog().info("------------------------------------------------------------------------");
        }
    }

    private Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        if (StringUtils.isBlank(getTest())) {
            configuration.setIncludes(getIncludes());
            configuration.setExcludes(getExcludes());
        } else {
            Set<String> include = new HashSet<String>();
            Set<String> exclude = new HashSet<String>();
            for (String test: getTest().split(",")) {
                test = test.trim();
                if (StringUtils.isNotBlank(test)) {
                    if (test.startsWith("!")) {
                        test = test.substring(1);
                        if (StringUtils.isNotBlank(test)) {
                            exclude.add(test);
                        }
                    } else {
                        include.add(test);
                    }
                }
            }
            configuration.setIncludes(include);
            configuration.setExcludes(exclude);
        }
        configuration.setClassesDirectory(getClassesDirectory());
        configuration.setTestClassesDirectory(getTestClassesDirectory());
        configuration.setReportsDirectory(getReportsDirectory());
        Set<String> groups = new HashSet<String>();
        for (String group: getGroups().split(",")) {
            groups.add(group.trim());
        }
        configuration.setGroups(groups);
        return configuration;
    }

    private void displayConfiguration(TestManager manager) {
        if (getLog().isInfoEnabled()) {
            getLog().info("Configuration:");
            getLog().info("    Include = "+manager.getConfiguration().getIncludes());
            getLog().info("    Exclude = "+manager.getConfiguration().getExcludes());
            getLog().info("     Groups = "+manager.getConfiguration().getGroups());
            getLog().info("   Parallel = false"); // TODO: gör så att man kan låta flera trådar köra samma test (kunna utföra lasttest)
            getLog().info("");
            getLog().info("Tests:");
            if (manager.getExecutionGroups().isEmpty()) {
                getLog().info("  No tests found");
            } else {
                for (TestGroup group: manager.getExecutionGroups()) {
                    getLog().info("  [" + group.getName()+"]");
                    for (Execution execution: group.getExecutions()) {
                        getLog().info("    "+execution.getName()+"("+execution.getPathGenerator().getSimpleName()+", "+execution.getStopCondition().getSimpleName()+", \""+execution.getStopConditionValue()+"\")");
                    }
                    getLog().info("");
                }
            }
            getLog().info("------------------------------------------------------------------------");
        }
    }

    private void executeTests(TestManager manager) {
        if (!manager.getExecutionGroups().isEmpty()) {
            for (TestGroup group: manager.getExecutionGroups()) {
                List<ExecutionContext> executionContexts = new ArrayList<ExecutionContext>();
                for (Execution execution: group.getExecutions()) {
                    try {
                        String value = execution.getStopConditionValue();
                        StopCondition stopCondition = execution.getStopCondition().getConstructor(String.class).newInstance(value);
                        PathGenerator pathGenerator = execution.getPathGenerator().getConstructor(StopCondition.class).newInstance(stopCondition);
                        Object implementation = execution.getTestClass().newInstance();
                        ExecutionContext executionContext = new ExecutionContext(execution.getModel(), pathGenerator);
                        implementations.put(executionContext, implementation);
                        executionContexts.add(executionContext);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                machines.add(new SimpleMachine(executionContexts));
            }
            try {
                ExecutorService executorService = Executors.newFixedThreadPool(machines.size());
                for (final Machine machine : machines) {
                    executorService.execute(new Runnable() {
                        public void run() {
                            while (machine.hasNextStep()) {
                                Element element = machine.getNextStep();
                                if (!"Start".equals(element.getName())) {
                                    ReflectionUtils.execute(implementations.get(machine.getCurrentExecutionContext())
                                        , element.getName(), machine.getCurrentExecutionContext().getScriptContext());
                                }
                            }
                        }
                    });
                }
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                throw new RuntimeException(ResourceUtils.getText(Bundle.NAME, "exception.execution.interrupted")); // TODO: byt exception
            }
        }
    }

    private void reportResults(TestManager manager) throws MojoExecutionException {

        boolean hasExceptions = false;
        for (Machine machine: machines) {
            //reportWriter.writeReport(graphWalker, reportsDirectory, session.getStartTime());
            for (ExecutionContext context: machine.getExecutionContexts()) {
                hasExceptions |= ExecutionStatus.FAILED.equals(context.getExecutionStatus());
            }
        }
        if (hasExceptions) {
            throw new MojoExecutionException(ResourceUtils.getText(Bundle.NAME, "exception.execution.failed", getReportsDirectory().getAbsolutePath()));
        }
    }

    private void displayResult(TestManager manager) {
        if (getLog().isInfoEnabled()) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info("");
            getLog().info(ResourceUtils.getText(Bundle.NAME, "result.label"));
            getLog().info("");
            long groups = manager.getGroupCount(), tests = manager.getTestCount(), completed = 0, failed = 0, notExecuted = 0;
            List<ExecutionContext> failedExecutions = new ArrayList<ExecutionContext>();
            for (Machine machine: machines) {
                for (ExecutionContext context: machine.getExecutionContexts()) {
                    switch (context.getExecutionStatus()) {
                        case COMPLETED: {
                            completed++;
                        }
                        break;
                        case FAILED: {
                            failed++;
                            failedExecutions.add(context);
                        }
                        break;
                        case NOT_EXECUTED: {
                            notExecuted++;
                        }
                        break;
                    }
                }
            }
            if (!failedExecutions.isEmpty()) {
                getLog().info("Failed executions: ");
                for (ExecutionContext context: failedExecutions) {
                    double fulfilment = context.getPathGenerator().getStopCondition().getFulfilment(context);
                    String pathGenerator = context.getPathGenerator().getClass().getSimpleName();
                    String stopCondition = context.getPathGenerator().getStopCondition().getClass().getSimpleName();
                    String value = context.getPathGenerator().getStopCondition().getValue();
                    getLog().info("  " + implementations.get(context).getClass().getName()+"("+pathGenerator+", "+stopCondition+", "+value+"): "+Math.round(100*fulfilment)+"%");
                }
                getLog().info("");
            }
            getLog().info(ResourceUtils.getText(Bundle.NAME, "result.summary", groups, tests, completed, failed, notExecuted));
            getLog().info("");
        }
    }
}
