package com.welovecoding.nbeditorconfig.listener;

import static com.welovecoding.nbeditorconfig.config.LoggerSettings.LISTENER_LOG_LEVEL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

public class ProjectOpenCloseListener extends ProjectOpenedHook {

  private static final Logger LOG = Logger.getLogger(ProjectOpenCloseListener.class.getName());
  private Project project;

  static {
    LOG.setLevel(LISTENER_LOG_LEVEL);
  }

  public ProjectOpenCloseListener() {
    super();
  }

  public ProjectOpenCloseListener(Project project) {
    this();
    this.project = project;
  }

  @Override
  protected void projectOpened() {
    FileObject projectFileObject = project.getProjectDirectory();
    LOG.log(Level.INFO, "Opened project: {0}", projectFileObject.getName());
    ListenerAttacher.attachListeners(projectFileObject, project);
  }

  @Override
  protected void projectClosed() {
    LOG.log(Level.FINE, "Closed project: {0}", project.getProjectDirectory().getName());
  }

}
