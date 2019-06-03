#!/usr/bin/env groovy

import com.aleibran.pipeline.providers.StageProvider
import com.aleibran.pipeline.providers.StageProviderDefault

def call(final Closure closure) {

  final def configuration = [:]
  closure.resolveStrategy = Closure.DELEGATE_FIRST
  closure.delegate = configuration
  closure()

  final List<String> setupBackingServicesCommands = configuration.setupBackingServices ?: [] as List<String>
  final List<String> buildCommands = configuration.build ?: [] as List<String>
  final List<String> cleanupCommands = configuration.cleanup ?: [] as List<String>

  node('maven-docker-pipeline') {
    final StageProvider stageProvider = new StageProviderDefault(this)

    stage("checkout") {
      stageProvider.checkoutStage()
    }
    stage("setup backing services") {
      stageProvider.setupBackingServicesStage(setupBackingServicesCommands)
    }
    stage("build artifact") {
      stageProvider.buildArtifactStage(buildCommands)
    }
    stage("build docker image") {
      stageProvider.buildDockerImageStage()
    }
    stage("push docker image") {
      stageProvider.pushDockerImageStage("dockerhub")
    }
    stage("cleanup") {
      stageProvider.cleanupStage(cleanupCommands)
    }
  }

}
