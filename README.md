# camunda-incident-handler-plugin
A camunda plugin that is supposed to handle incident creation to create a workflow instance that could for example report the incident in a incident management tool

# Wildfly
This is also supposed to be used in the camunda wildfly distribution. A maven assembly plugin is used to produce a zip that can be unzipped in the module folder. In the module.xml it is stated what camunda module has to be updated to add a dependency to the created module.
