package com.aleibran.pipeline.models

class BackingService implements Serializable {

  String name
  String host
  long port
  int retries
  int timeout

}
