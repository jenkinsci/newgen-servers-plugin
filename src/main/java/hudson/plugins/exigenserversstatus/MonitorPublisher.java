package hudson.plugins.newgenserversstatus;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class MonitorPublisher extends Notifier {

	private String regexpPattern;
	private String serverHost;
	private String serverPort;
	private String serverAppName;

	public MonitorPublisher(String regexpPattern, String serverHost,
			String serverPort, String serverAppName) {
		this.regexpPattern = regexpPattern;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.serverAppName = serverAppName;
	}

	@Extension
	public static final MonitorDescriptor DESCRIPTOR = new MonitorDescriptor();

	@Override
	public boolean needsToRunAfterFinalized() {
		return false;
	}


	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return null;
	}


	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		installMonitor();
		return true;
	}


	/**
	 * Installs MonitorAction onto the front page. If it is already  installed, nothing happens.
	 */
	private void installMonitor() {
		boolean isInstalled = false;
		List<Action> installedActions = Hudson.getInstance().getActions();
		for (Action installedAction: installedActions) {
			if (installedAction instanceof MonitorAction) {
				isInstalled = true;
				break;
			}
		}
		if (!isInstalled) {
			MonitorAction action = new MonitorAction();
			Hudson.getInstance().getActions().add(action);
		}
	}

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getRegexpPattern() {
		if (StringUtils.isEmpty(regexpPattern)) {
			return DESCRIPTOR.getDefaultRegexpPattern();
		}
		return regexpPattern;
	}
	
	public void setRegexpPattern(String regexpPattern) {
		this.regexpPattern = regexpPattern;
	}


	public String getServerAppName() {
		return serverAppName;
	}


	public void setServerAppName(String serverAppName) {
		this.serverAppName = serverAppName;
	}
}
