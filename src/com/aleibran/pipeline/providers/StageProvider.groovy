package com.aleibran.pipeline.providers

interface StageProvider {

  void checkoutStage();

  void setupBackingServicesStage(List<String> commands);

  void buildArtifactStage(List<String> commands);

  void buildDockerImageStage();

  void pushDockerImageStage(String dockerRegistryCredentialsId);

  void cleanupStage(List<String> commands);

}

