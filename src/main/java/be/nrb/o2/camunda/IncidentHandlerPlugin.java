package be.nrb.o2.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.slf4j.LoggerFactory;

public class IncidentHandlerPlugin extends AbstractProcessEnginePlugin {

  private static final CreateIMIncidentHandler HISTORY_EVENT_HANDLER = new CreateIMIncidentHandler();

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LoggerFactory.getLogger(this.getClass()).warn("This plugin is preInit...");
    CompositeHistoryEventHandler historyEventHandler = new CompositeHistoryEventHandler();
    HistoryEventHandler previousHEH = processEngineConfiguration.getHistoryEventHandler();
    LoggerFactory.getLogger(this.getClass()).warn("The existing HistoryEventHandler is {}", previousHEH);
    if(previousHEH!=null){
      historyEventHandler.add(previousHEH);
    }else{
      historyEventHandler = new CompositeDbHistoryEventHandler();
    }
    historyEventHandler.add(HISTORY_EVENT_HANDLER);
    processEngineConfiguration.setHistoryEventHandler(historyEventHandler);
    LoggerFactory.getLogger(this.getClass()).warn("The new HistoryEventHandler is {}", historyEventHandler);
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    HISTORY_EVENT_HANDLER.setProcessEngine(processEngine);
  }
  
  
}
