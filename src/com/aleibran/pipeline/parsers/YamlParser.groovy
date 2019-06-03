package com.aleibran.pipeline.parsers

import com.aleibran.pipeline.models.DockerBuildAttributes
import com.aleibran.pipeline.models.ServiceDependency

interface YamlParser {

  ServiceDependency parseWaitForItServiceDependencyFile(String filePath);

  DockerBuildAttributes parseDockerBuildAttributesFile(String filePath);

}
