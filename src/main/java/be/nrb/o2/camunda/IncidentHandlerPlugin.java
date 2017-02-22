package be.nrb.o2.camunda;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.slf4j.LoggerFactory;

public class IncidentHandlerPlugin extends AbstractProcessEnginePlugin {

  private static final CreateIMIncidentHandler HISTORY_EVENT_HANDLER = new CreateIMIncidentHandler();
  
  private boolean enabled = true;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    CompositeHistoryEventHandler historyEventHandler = new CompositeHistoryEventHandler();
    HistoryEventHandler previousHEH = processEngineConfiguration.getHistoryEventHandler();
    LoggerFactory.getLogger(this.getClass()).debug("The existing HistoryEventHandler is {}", previousHEH);
    if(previousHEH!=null){
      historyEventHandler.add(previousHEH);
    }else{
      historyEventHandler = new CompositeDbHistoryEventHandler();
    }
    historyEventHandler.add(HISTORY_EVENT_HANDLER);
    processEngineConfiguration.setHistoryEventHandler(historyEventHandler);
    LoggerFactory.getLogger(this.getClass()).debug("The new HistoryEventHandler is {}", historyEventHandler);
    
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
      ObjectName name = new ObjectName("be.nrb.o2.camunda:type=CreateIMIncidentHandler"); 
      mbs.registerMBean(HISTORY_EVENT_HANDLER, name);
    } catch (Throwable e) {
      LoggerFactory.getLogger(this.getClass()).error("Impossible to register MBean", e);
    } 
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    HISTORY_EVENT_HANDLER.setProcessEngine(processEngine);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    HISTORY_EVENT_HANDLER.setEnabled(enabled);
  }
  
  
  
  
}
