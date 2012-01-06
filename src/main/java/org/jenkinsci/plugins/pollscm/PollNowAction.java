/*
 * The MIT License
 * 
 * Copyright (c) 2012, Vincent Latombe
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
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import hudson.triggers.Trigger;
import hudson.triggers.SCMTrigger;

import java.util.Collection;
import java.util.Collections;

public class PollNowAction implements Action {
	@Extension
	public static class TransientProjectActionFactoryImpl extends TransientProjectActionFactory {

		@Override
		public Collection<? extends Action> createFor(AbstractProject target) {
			Trigger trigger = target.getTrigger(SCMTrigger.class);
			if (trigger != null) {
				return Collections.singleton(new PollNowAction(target));
			} else {
				return Collections.EMPTY_LIST;
			}
		}

	}
	
	private AbstractProject target;

	public PollNowAction(AbstractProject target) {
		this.target = target;
	}
	
	public Trigger getTrigger() {
		return target.getTrigger(SCMTrigger.class);
	}
	
	public AbstractProject getOwner() {
		return target;
	}

	public String getIconFileName() {
		return "/plugin/pollscm/images/24x24/clipboard-play.png";
	}

	public String getDisplayName() {
		return Messages.PollNowAction_PollNow();
	}

	public String getUrlName() {
		return "poll";
	}
	
	public static final Permission POLL = new Permission(Item.PERMISSIONS, "Poll", Messages._PollNowAction_PollPermission_Description(),  Permission.UPDATE, PermissionScope.ITEM);

}
