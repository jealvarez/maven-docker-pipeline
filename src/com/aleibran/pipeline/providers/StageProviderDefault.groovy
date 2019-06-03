package com.aleibran.pipeline.providers

import com.aleibran.pipeline.models.DockerBuildAttributes
import com.aleibran.pipeline.models.ServiceDependency
import com.aleibran.pipeline.parsers.YamlParser
@Grapes([
        @Grab("org.yaml:snakeyaml:1.23"),
        @Grab("org.apache.commons:commons-lang3:3.8.1")
])
import com.aleibran.pipeline.parsers.YamlParserDefault
import org.jenkinsci.plugins.workflow.cps.CpsScript

class StageProviderDefault implements StageProvider, Serializable {

  private final YamlParser yamlParser
  private final CpsScript cpsScript
  private final String workspacePath
  private dockerImageBuilt = null

  StageProviderDefault(final CpsScript cpsScript) {
    this.cpsScript = cpsScript
    this.yamlParser = new YamlParserDefault(cpsScript)
    this.workspacePath = cpsScript.env.WORKSPACE
  }

  @Override
  void checkoutStage() {
    cpsScript.step([$class: 'WsCleanup'])
    cpsScript.checkout([
            $class           : 'GitSCM',
            branches         : cpsScript.scm.branches,
            extensions       : cpsScript.scm.extensions + [
                    [$class: 'CleanBeforeCheckout'],
                    [
                            $class             : 'SubmoduleOption',
                            disableSubmodules  : false,
                            parentCredentials  : true,
                            recursiveSubmodules: false,
                            reference          : '',
                            timeout            : 60,
                            trackingSubmodules : false
                    ],
                    [
                            $class      : 'CloneOption',
                            depth       : 30,
                            honorRefspec: true,
                            noTags      : true,
                            reference   : '',
                            shallow     : true
                    ],
                    [
                            $class: 'LocalBranch'
                    ],
            ],
            userRemoteConfigs: cpsScript.scm.userRemoteConfigs
    ])
    cpsScript.echo "Current Branch: ${cpsScript.env.BRANCH_NAME}"
  }

  @Override
  void setupBackingServicesStage(final List<String> commands) {
    final ServiceDependency serviceDependency = yamlParser.parseWaitForItServiceDependencyFile("$workspacePath/waitforit.yml")

    commands.each {
      final command -> cpsScript.sh command
    }
    serviceDependency.backingServices.each { final backingService -> cpsScript.sh "waitforit -host=$backingService.host -port=$backingService.port -timeout=$backingService.timeout -retry=$backingService.retries -debug" }
  }

  @Override
  void buildArtifactStage(final List<String> commands) {
    commands.each {
      final command -> cpsScript.sh command
    }
  }

  @Override
  void buildDockerImageStage() {
    final DockerBuildAttributes dockerBuildAttributes = yamlParser.parseDockerBuildAttributesFile("$workspacePath/.docker-build") as DockerBuildAttributes
    final def dockerImageTag = cpsScript.env.BRANCH_NAME.replaceAll("/", "_")

    dockerImageBuilt = cpsScript.docker.build("${dockerBuildAttributes.imageName}:$dockerImageTag", "-f docker/Dockerfile .")
  }

  @Override
  void pushDockerImageStage(final String dockerRegistryCredentialsId) {
    final DockerBuildAttributes dockerBuildAttributes = yamlParser.parseDockerBuildAttributesFile("$workspacePath/.docker-build") as DockerBuildAttributes

    cpsScript.docker.withRegistry("https://${dockerBuildAttributes.registryUrl}", "$dockerRegistryCredentialsId") {
      dockerImageBuilt.push()
    }
  }

  @Override
  void cleanupStage(final List<String> commands) {
    final DockerBuildAttributes dockerBuildAttributes = yamlParser.parseDockerBuildAttributesFile("$workspacePath/.docker-build") as DockerBuildAttributes

    commands.each {
      final command -> cpsScript.sh command
    }
    cpsScript.sh "docker rmi ${dockerImageBuilt.id}"
    cpsScript.sh "docker rmi ${dockerBuildAttributes.registryUrl}/$dockerImageBuilt.id"
    cpsScript.step([$class: 'WsCleanup'])
  }

}
