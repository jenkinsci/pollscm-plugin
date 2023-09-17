package org.jenkinsci.plugins.pollscm;

import hudson.Messages;
import hudson.security.ACL;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import jenkins.triggers.SCMTriggerItem;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollNowActionTest {
    private PollNowAction mockPollNowAction;
    private SCMTriggerItem mockScmTriggerItem;
    private SCMTrigger mockScmTrigger;
    private ACL acl;
    private Jenkins jenkins;

    @Rule
    JenkinsRule jenkinsRule = new JenkinsRule();

    @BeforeEach
    void setup(){
        mockScmTriggerItem = mock(SCMTriggerItem.class);
        mockScmTrigger = mock(SCMTrigger.class);
        jenkins = mock(Jenkins.class);
        acl = mock(ACL.class);

        when(jenkins.getACL()).thenReturn(acl);
        when(mockScmTriggerItem.getSCMTrigger()).thenReturn(mockScmTrigger);
        mockPollNowAction = new PollNowAction(mockScmTriggerItem);
    }

    @Test
    void getTrigger() {
        Trigger trigger = mockPollNowAction.getTrigger();
        assertEquals(mockScmTrigger,trigger);
    }

    @Test
    void getOwner() {
        SCMTriggerItem owner = mockPollNowAction.getOwner();
        assertEquals(mockScmTriggerItem,owner);
    }

    @Test
    void getIconFileNameWithPermission() {
        try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class, CALLS_REAL_METHODS)){
            jenkinsMockedStatic.when(()-> Jenkins.getInstance()).thenReturn(jenkins);
            when(acl.hasPermission(PollNowAction.POLL)).thenReturn(true);
            String iconName = mockPollNowAction.getIconFileName();
            assertEquals("/plugin/pollscm/images/24x24/clipboard-play.png",iconName);
        }
    }

    @Test
    void getIconFileNameWithoutPermission() {
        try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class, CALLS_REAL_METHODS)){
            jenkinsMockedStatic.when(()-> Jenkins.getInstance()).thenReturn(jenkins);
            when(acl.hasPermission(PollNowAction.POLL)).thenReturn(false);
            String iconName = mockPollNowAction.getIconFileName();
            assertEquals(null,iconName);
        }
    }

    @Test
    @WithoutJenkins
    void getIconFileNameIllegalExceptionHandler(){
        try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class, CALLS_REAL_METHODS)){
            assertThrows(IllegalStateException.class,()->mockPollNowAction.getIconFileName());
        }
    }

    @Test
    void getDisplayName() {
        String displayName = mockPollNowAction.getDisplayName();
        assertEquals("Poll Now", displayName);
    }

    @Test
    void getUrlName() {
        String url = mockPollNowAction.getUrlName();
        assertEquals("poll",url);
    }

    @Test
    void doPolling() {
        try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class, CALLS_REAL_METHODS)){
            jenkinsMockedStatic.when(()-> Jenkins.getInstance()).thenReturn(jenkins);
            StaplerRequest req = mock(StaplerRequest.class);
            StaplerResponse res = mock(StaplerResponse.class);
            doNothing().when(mockScmTrigger).run();

            assertDoesNotThrow(()->mockPollNowAction.doPolling(req,res));
            verify(mockScmTrigger,times(1)).run();
        }
    }


    @Test
    void doPollingThatThrowsException() {
        try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class, CALLS_REAL_METHODS)){
            jenkinsMockedStatic.when(()-> Jenkins.getInstance()).thenReturn(jenkins);
            StaplerRequest req = mock(StaplerRequest.class);
            StaplerResponse res = mock(StaplerResponse.class);
            when(mockPollNowAction.getTrigger()).thenReturn(null);

            assertThrows(IllegalStateException.class,()->mockPollNowAction.doPolling(req,res));
        }
    }

    @Test
    void createForWithException(){
        PollNowAction.TransientProjectActionFactoryImpl transientProjectActionFactory = new PollNowAction.TransientProjectActionFactoryImpl();
        when(mockScmTriggerItem.getSCMTrigger()).thenReturn(null);
        Collection collection = transientProjectActionFactory.createFor(mockScmTriggerItem);

        assertEquals(Collections.EMPTY_LIST, collection);
    }

    @Test
    void createForWithoutException(){
        PollNowAction.TransientProjectActionFactoryImpl transientProjectActionFactory = new PollNowAction.TransientProjectActionFactoryImpl();
        when(mockScmTriggerItem.getSCMTrigger()).thenReturn(mockScmTrigger);
        Collection collection = transientProjectActionFactory.createFor(mockScmTriggerItem);

        assertEquals(1, collection.size());
    }

    @Test
    void getClassTest(){
        PollNowAction.TransientProjectActionFactoryImpl transientProjectActionFactory = new PollNowAction.TransientProjectActionFactoryImpl();
        assertEquals(SCMTriggerItem.class,transientProjectActionFactory.type());
    }
}