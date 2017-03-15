package be.nrb.o2.camunda;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.slf4j.LoggerFactory;

public class CreateIMIncidentHandler implements HistoryEventHandler, CreateIMIncidentHandlerMBean {

  private String incidentWorkflowKey = "CreateIM";
  private ProcessEngine processEngine;
  private boolean enabled = true;

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    if(historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_CREATE)){
      if (historyEvent instanceof HistoricIncidentEventEntity) {
        if(processEngine!=null){
            if (enabled) {
              boolean skip = false;
              String processDefinitionId = historyEvent.getProcessDefinitionId();
              if (processDefinitionId!=null && processDefinitionId.length()>0) {
                BpmnModelInstance modelInstance = processEngine.getRepositoryService().getBpmnModelInstance(processDefinitionId);
                Process process = (Process) modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);
                ExtensionElements extensionElements = process.getExtensionElements();
                if (extensionElements != null) {
                  CamundaProperties camundaProperties = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).singleResult();
                  for (CamundaProperty camundaProperty : camundaProperties.getCamundaProperties()) {
                    LoggerFactory.getLogger(this.getClass()).info("Checking process extension camunda property {}", camundaProperty.getCamundaName());
                    if (camundaProperty.getCamundaName().equals("skipIncident")) {
                      LoggerFactory.getLogger(this.getClass()).info("The skipIncident is {}", camundaProperty.getCamundaValue());
                      skip = Boolean.parseBoolean(camundaProperty.getCamundaValue());
                    }
                  }
                } else {
                  LoggerFactory.getLogger(this.getClass()).info("The process model doesn't contain any extension element");
                }
                LoggerFactory.getLogger(this.getClass()).info("The skip variable is {}", skip);
                if (!skip) {

                  long createIMCount = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(incidentWorkflowKey).active().count();

                  if (createIMCount > 0) {
                    HistoricIncidentEventEntity incidentEventEntity = (HistoricIncidentEventEntity) historyEvent;
                    String incidentMessage = incidentEventEntity.getIncidentMessage();
                    String processInstanceId = incidentEventEntity.getProcessInstanceId();
                    if (incidentMessage == null || incidentMessage.length() == 0) {
                      incidentMessage = String.format("A camunda incident have occured in process %s", processInstanceId);
                    }
                    LoggerFactory.getLogger(this.getClass()).warn("We should create an HPSM Incident here with message : {} for process {}", incidentMessage, processInstanceId);
                    try {
                      HashMap<String, Object> initialVariables = new HashMap<>();
                      initialVariables.put("message", incidentMessage);
                      initialVariables.put("processInstanceId", processInstanceId);
                      initialVariables.put("incidentEventEntity", incidentEventEntity);
                      ProcessInstance incidentProcesss = processEngine.getRuntimeService().startProcessInstanceByKey(incidentWorkflowKey, initialVariables);
                      LoggerFactory.getLogger(this.getClass()).info("Created {} process {}", incidentWorkflowKey, incidentProcesss.getProcessInstanceId());
                    } catch (Throwable e) {
                      LoggerFactory.getLogger(this.getClass()).error("Failed to Created " + incidentWorkflowKey + " process", e);
                    }
                  }
                } 
              } 
            }else{
              LoggerFactory.getLogger(this.getClass()).info("An incident occured but IncidentHandlerPlugin is not enabled");
            }
        }else{
          LoggerFactory.getLogger(this.getClass()).error("Trying to handle an incident without having processEngine availble");
        }
      }
    }
    
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    for (HistoryEvent historyEvent : historyEvents) {
      handleEvent(historyEvent);
    }
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public String getIncidentWorkflowKey() {
    return incidentWorkflowKey;
  }

  public void setIncidentWorkflowKey(String incidentWorkflowKey) {
    this.incidentWorkflowKey = incidentWorkflowKey;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  

}
