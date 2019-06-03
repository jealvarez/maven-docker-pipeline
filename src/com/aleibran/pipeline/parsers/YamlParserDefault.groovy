package com.aleibran.pipeline.parsers

import com.aleibran.pipeline.models.DockerBuildAttributes
import com.aleibran.pipeline.models.ServiceDependency
import org.apache.commons.lang3.StringUtils
import org.jenkinsci.plugins.workflow.cps.CpsScript
import org.yaml.snakeyaml.Yaml

class YamlParserDefault implements YamlParser, Serializable {

  private final CpsScript cpsScript

  YamlParserDefault(final CpsScript cpsScript) {
    this.cpsScript = cpsScript
  }

  @Override
  ServiceDependency parseWaitForItServiceDependencyFile(final String filePath) {
    final String fileContent = cpsScript.readFile(filePath)

    if (StringUtils.isBlank(fileContent)) {
      return
    }

    final Yaml yamlParser = new Yaml()
    final ServiceDependency serviceDependency = yamlParser.load(fileContent)

    return serviceDependency
  }

  @Override
  DockerBuildAttributes parseDockerBuildAttributesFile(final String filePath) {
    final String fileContent = cpsScript.readFile(filePath)

    if (StringUtils.isBlank(fileContent)) {
      return
    }

    final Yaml yamlParser = new Yaml()
    final DockerBuildAttributes dockerBuildAttributes = yamlParser.load(fileContent)

    return dockerBuildAttributes
  }

}
