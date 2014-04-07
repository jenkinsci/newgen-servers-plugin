package hudson.plugins.newgenserversstatus;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;


public class MonitorDescriptor extends BuildStepDescriptor<Publisher> {

	public static final String ACTION_LOGO_LARGE = "/plugin/newgen-servers/icons/monitor-32x32.png";
	public static final String ACTION_LOGO_MEDIUM = "/plugin/newgen-servers/icons/monitor-22x22.png";
	
	/**
	 * For name in this format: servername-serverport-jobname
	 */
	public static final String DEFAULT_REGEXP_PATTERN = "(.*)\\-(.*)\\-.*";

	protected MonitorDescriptor() {
		super(MonitorPublisher.class);
		load();
	}

	@Override
	public String getDisplayName() {
		return "Servers Status";
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        	return true;
	}

	@Override
	public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		return new MonitorPublisher(req.getParameter("ess.regexpPattern"), req.getParameter("ess.serverHost"), req.getParameter("ess.serverPort"), req.getParameter("ess.serverAppName"));
	}
	
	public String getDefaultRegexpPattern() {
	      return DEFAULT_REGEXP_PATTERN;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json)
			throws hudson.model.Descriptor.FormException {
		save();
		return super.configure(req, json);
	}

}
