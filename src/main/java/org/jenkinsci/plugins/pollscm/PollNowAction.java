/*
 * The MIT License
 *
 * Copyright (c) 2012-2017, Vincent Latombe
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
package org.jenkinsci.plugins.pollscm;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import hudson.triggers.Trigger;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;
import jenkins.triggers.SCMTriggerItem;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PollNowAction implements Action {
    public static final Permission POLL = new Permission(
            Item.PERMISSIONS,
            "Poll",
            Messages._PollNowAction_PollPermission_Description(),
            Permission.UPDATE,
            PermissionScope.ITEM);
    private SCMTriggerItem target;

    public PollNowAction(SCMTriggerItem target) {
        this.target = target;
    }

    public Trigger getTrigger() {
        return target.getSCMTrigger();
    }

    public SCMTriggerItem getOwner() {
        return target;
    }

    public String getIconFileName() {
        return getACL().hasPermission(POLL) ? "/plugin/pollscm/images/24x24/clipboard-play.png" : null;
    }

    private ACL getACL() {
        Jenkins j = Jenkins.getInstance();
        if (j == null) {
            throw new IllegalStateException("Jenkins is null");
        } else {
            return j.getACL();
        }
    }

    public String getDisplayName() {
        return Messages.PollNowAction_PollNow();
    }

    public String getUrlName() {
        return "poll";
    }

    /**
     * Schedules a new SCM polling command.
     */
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doPolling(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        getACL().checkPermission(POLL);
        Trigger trigger = getTrigger();
        if (trigger != null) {
            trigger.run();
        } else {
            throw new IllegalStateException("Trigger is null");
        }
        rsp.sendRedirect(".");
    }

    @Extension
    public static class TransientProjectActionFactoryImpl extends TransientActionFactory<SCMTriggerItem> {

        @Override
        public Collection<? extends Action> createFor(SCMTriggerItem target) {
            Trigger trigger = target.getSCMTrigger();
            if (trigger != null) {
                return Collections.singleton(new PollNowAction(target));
            }
            return Collections.EMPTY_LIST;
        }

        @Override
        public Class type() {
            return SCMTriggerItem.class;
        }
    }
}
