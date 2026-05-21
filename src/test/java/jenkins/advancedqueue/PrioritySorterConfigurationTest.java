/*
 * The MIT License
 *
 * Copyright 2026 Mark Waite.
 *
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
 */
package jenkins.advancedqueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Queue;
import hudson.util.FormValidation;
import jenkins.advancedqueue.sorter.SorterStrategy;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import jenkins.advancedqueue.sorter.strategy.MultiBucketStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class PrioritySorterConfigurationTest {

    private static JenkinsRule j;
    private static SorterStrategy originalStrategy;
    private static boolean originalEditConfiguration;

    private PrioritySorterConfiguration config;

    public PrioritySorterConfigurationTest() {}

    @BeforeAll
    static void setStatics(JenkinsRule rule) throws Exception {
        j = rule;
        PrioritySorterConfiguration c = PrioritySorterConfiguration.get();
        originalStrategy = c.getStrategy();
        originalEditConfiguration = c.getOnlyAdminsMayEditPriorityConfiguration();
    }

    @BeforeEach
    public void resetGlobalConfig() throws Exception {
        config = PrioritySorterConfiguration.get();
        config.setStrategy(originalStrategy);
        config.setOnlyAdminsMayEditPriorityConfiguration(originalEditConfiguration);
        config.save();
    }

    @Test
    void testInit() {
        PrioritySorterConfiguration.init();
        assertThat(config.getStrategy().getNumberOfPriorities(), is(5));
        assertThat(config.getStrategy().getDefaultPriority(), is(3));
        assertFalse(config.getOnlyAdminsMayEditPriorityConfiguration());
    }

    @Test
    void testdoCheckNumberOfPriorities() {
        String errorMessage = FormValidation.error(Messages.PrioritySorterConfiguration_enterValueRequestMessage())
                .toString();
        assertThat(config.doCheckNumberOfPriorities("3"), is(FormValidation.ok()));
        assertThat(config.doCheckNumberOfPriorities("").toString(), is(errorMessage));
        assertThat(config.doCheckNumberOfPriorities("-371").toString(), is(errorMessage));
        assertThat(config.doCheckNumberOfPriorities("12.34").toString(), is(errorMessage));
    }

    @Test
    void testGetOnlyAdminsMayEditPriorityConfiguration() {
        assertFalse(config.getOnlyAdminsMayEditPriorityConfiguration());
    }

    @Test
    void testGetStrategy() {
        assertThat(config.getStrategy().getNumberOfPriorities(), is(5));
        assertThat(config.getStrategy().getDefaultPriority(), is(3));
    }

    @Test
    void testSetOnlyAdminsMayEditPriorityConfiguration() {
        assertFalse(config.getOnlyAdminsMayEditPriorityConfiguration());
        config.setOnlyAdminsMayEditPriorityConfiguration(true);
        assertTrue(config.getOnlyAdminsMayEditPriorityConfiguration());
    }

    @Test
    void testSetStrategy() {
        assertThat(config.getStrategy().getNumberOfPriorities(), is(5));
        assertThat(config.getStrategy().getDefaultPriority(), is(3));
        SorterStrategy strategy = new SorterStrategyImpl();
        config.setStrategy(strategy);
        assertThat(config.getStrategy().getNumberOfPriorities(), is(strategy.getNumberOfPriorities()));
        assertThat(config.getStrategy().getDefaultPriority(), is(strategy.getDefaultPriority()));
    }

    /* A sorter strategy that is intentionally different than the default */
    private static class SorterStrategyImpl extends SorterStrategy {

        private final int NUMBER_OF_PRIORITIES = 4 + MultiBucketStrategy.DEFAULT_PRIORITIES_NUMBER;

        public SorterStrategyImpl() {}

        @Override
        public SorterStrategyCallback onNewItem(Queue.Item item, SorterStrategyCallback weightCallback) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getNumberOfPriorities() {
            return NUMBER_OF_PRIORITIES;
        }

        @Override
        public int getDefaultPriority() {
            return NUMBER_OF_PRIORITIES / 2;
        }
    }
}
