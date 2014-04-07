package hudson.plugins.exigenserversstatus;

import hudson.Extension;
import hudson.model.*;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.jsp.jstl.core.LoopTagStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Status Monitor, shows the configured Jobs in a single screen overview
 * 
 * @author astasauskas
 */
@ExportedBean (defaultVisibility = 999)
@Extension
public class MonitorAction implements RootAction {
	

    private static final long serialVersionUID = 1L;

	private static final int COLUMNS = 1;


	public String getDisplayName() {
		// The Name on the Dashboard
		return "Servers Status";
	}


	public String getIconFileName() {
		return MonitorDescriptor.ACTION_LOGO_MEDIUM;
	}


	public String getUrlName() {
		// The name of the URL path segment
		return "/exigenservers";
	}


	/**
	 * @return list projects that will be displayed
	 */
	private List<AbstractProject> getProjects() {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
		List<TopLevelItem> topLevelItems = Hudson.getInstance().getItems();
        for (TopLevelItem topLevelItem : topLevelItems) {
            if (topLevelItem instanceof AbstractProject) {
                AbstractProject abstractProject = (AbstractProject) topLevelItem;
                if (abstractProject.getPublishersList().get(MonitorPublisher.DESCRIPTOR) != null) {
                        result.add(abstractProject);
                }
            }
        }

		return result;
	}


	public String getResult(AbstractProject project) {
		String result;
		if ((project.getLastCompletedBuild() != null) && (project.getLastCompletedBuild().getResult() != null)) {
			if (project.isDisabled()) {
				result = "DISABLED";
			}
			else {
				result = project.getLastCompletedBuild().getResult().toString();
			}
		}
		else {
			result = "NOT_BUILD";
		}
		return result;
	}


//	private int getRows() {
//		int size = getProjects().size();
//		if (size <= 3) {
//			return size;
//		}
//		return ((size % COLUMNS) == 0) ? (size / COLUMNS) : ((size + 1) / COLUMNS);
//	}
	
	/**
	 * Find out the server
	 * For projects are named according to default regexp or entered, name is parsed against the regex string
	 * For projects where server host and port are defined, they are taken to use
	 * 
	 * @param project
	 */
	@Exported
	public ServerIdentifier findServer(AbstractProject project) {
		if (project.getName() == null) {
			return new ServerIdentifier("unknown", "????", "");
		}

		if (!StringUtils.isEmpty(getHostFromProject(project)) && !StringUtils.isEmpty(getPortFromProject(project))) {
			return new ServerIdentifier(getHostFromProject(project), getPortFromProject(project), getAppNameFromProject(project));
		} else if (!StringUtils.isEmpty(getRegexpFromProject(project))) {
			Pattern pattern = Pattern.compile(getRegexpFromProject(project));
			Matcher matcher = pattern.matcher(project.getName());
			if (matcher.matches()) {
				return new ServerIdentifier(matcher.group(1), matcher.group(2), getAppNameFromProject(project));
			} else {
				return new ServerIdentifier("unknown", "????", "");
			}
		} else {
	    	return new ServerIdentifier("unknown", "????", "");
	    }
	}

	private String getHostFromProject(AbstractProject project) {
		return ((MonitorPublisher)project.getPublishersList().get(MonitorPublisher.DESCRIPTOR)).getServerHost();
	}

	private String getPortFromProject(AbstractProject project) {
		return ((MonitorPublisher)project.getPublishersList().get(MonitorPublisher.DESCRIPTOR)).getServerPort();
	}
	
	private String getRegexpFromProject(AbstractProject project) {
		return ((MonitorPublisher)project.getPublishersList().get(MonitorPublisher.DESCRIPTOR)).getRegexpPattern();
	}
	
	private String getAppNameFromProject(AbstractProject project) {
		String appName = ((MonitorPublisher)project.getPublishersList().get(MonitorPublisher.DESCRIPTOR)).getServerAppName();
		if (appName == null) {
			appName = "";
		}
		return appName;
	}
	
	/**
	 * Get column count
	 * 
	 * @return
	 */
    private int getRows() {
        List<AbstractProject> projects = getProjects();
        List<ServerIdentifier> servers = extractServersList(projects);
        return servers.size();
        //return 2;
    }
    
    /**
     * Get column count
     * 
     * @return
     */
    private int getProjectsInServerCount(AbstractProject project) {
        List<AbstractProject> projects = getProjects();
        List<ServerIdentifier> servers = extractServersList(projects);
        for (ServerIdentifier server: servers) {
            if (server.compareTo(findServer(project)) == 0) {
                return server.getProjectCount();
            }
        }
        return -1;
    }


    private List<ServerIdentifier> extractServersList(List<AbstractProject> projects) {
        List<ServerIdentifier> servers = new ArrayList<ServerIdentifier>();
        for (AbstractProject project: projects) {
            ServerIdentifier server = findServer(project);
            ServerIdentifier found = null;
            for (ServerIdentifier existingServer: servers) {
                if (server.compareTo(existingServer) == 0) {
                    found = existingServer;
                    break;
                }
            }
            if (found == null) { 
                server.setNumber(servers.size());
                servers.add(server);
            } else {
                found.setProjectCount(found.getProjectCount() + 1);
            }
        }
        return servers;
    }


	@Exported
	public double getRowsHeight() {
		return 100 / new Double(getRows());
	}


//	@Exported
//	public AbstractProject[][] getProjectsArray() {
//		int rows = getRows();
//		AbstractProject[][] result = new AbstractProject[rows][];
//		List<AbstractProject> projects = getProjects();
//		for (int i = 0; i < rows; i++) {
//			AbstractProject[] row = result[i];
//			if (row == null) {
//				if (projects.size() <= 3) {
//					row = new AbstractProject[1];
//					row[0] = projects.get(i);
//					//row[0].setDescription(description)
//				}
//				else {
//					// last row and uneven
//					if (((i + 1) == rows) && ((projects.size() % 2) != 0)) {
//						row = new AbstractProject[1];
//						row[0] = projects.get(i * COLUMNS);
//					}
//					else {
//						row = new AbstractProject[COLUMNS];
//						for (int j = 0; j < COLUMNS; j++) {
//							row[j] = projects.get((i * COLUMNS) + j);
//						}
//					}
//				}
//				result[i] = row;
//			}
//		}
//		return result;
//	}
	
    @Exported
    public AbstractProject[][] getProjectsArray() {
        int rows = getRows();
        AbstractProject[][] result = new AbstractProject[rows][];
        List<AbstractProject> projects = getProjects();
        List<ServerIdentifier> servers = extractServersList(projects);
        for (int i = 0; i < servers.size(); i++) {
            AbstractProject[] row = result[i];
            if (row == null) {
                /*if (projects.size() <= 3) {
                    row = new AbstractProject[1];
                    row[0] = projects.get(i);
                }
                else {
                    // last row and uneven
                    if (((i + 1) == rows) && ((projects.size() % 2) != 0)) {
                        row = new AbstractProject[1];
                        row[0] = projects.get(i * COLUMNS);
                    }
                    else {
                        row = new AbstractProject[COLUMNS];
                        for (int j = 0; j < COLUMNS; j++) {
                            row[j] = projects.get((i * COLUMNS) + j);
                        }
                    }
                }*/
                int projectsInServer = getProjectsInServerCount(projects.get(i));  
                row = new AbstractProject[projectsInServer];
                //row = new AbstractProject[1];
                int count = 0;
                for (AbstractProject project: projects) {
                    if (findServer(project).compareTo(servers.get(i)) == 0) {
                        row[count] = project;
                        count++;
                    }
                }
                result[i] = row;
            }
        }
        return result;
    }

	@Exported
	public int getStyleId(LoopTagStatus varStatus, AbstractProject[][] projectsArray) {
		boolean lastLine = varStatus.isLast() && (projectsArray.length > 1) && (projectsArray[projectsArray.length - 1].length == 1);
		boolean oneDimenional = (projectsArray[0].length == 1);
		if (oneDimenional || lastLine) {
			return 1;
		}
		return 2;
	}
	

    public static class ServerIdentifier implements Comparable<ServerIdentifier> {
        private String host;
        private String port;
        private String appName;
        private int number;
        private int projectCount;
        
        public ServerIdentifier(String host, String port, String appName) {
            this.host = host;
            this.port = port;
            this.appName = appName;
            this.projectCount = 1;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getProjectCount() {
            return projectCount;
        }

        public void setProjectCount(int projectCount) {
            this.projectCount = projectCount;
        }

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public int compareTo(ServerIdentifier other) {
			if (other.getHost().equals(getHost()) &&
	            other.getPort().equals(getPort()) &&
	            other.getAppName().equals(getAppName())) {
				return 0;
			}
			return -1;
		}
    }
}
