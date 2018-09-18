package org.coursera.metrics.datadog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class AwsHelper {

  public static final String url = "http://169.254.169.254/latest/meta-data/instance-id";

  // ECS details require the ecs config ECS_ENABLE_CONTAINER_METADATA=true on the underlying instance
  private static final String ecs_metadata_file_env = "ECS_CONTAINER_METADATA_FILE";
  private static final ObjectMapper objectMapper = new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);

  public static String getEc2InstanceId() throws IOException {
    try {
      return Request.Get(url).execute().returnContent().asString();
    } catch (Throwable t) {
      throw new IOException(t);
    }
  }

  public static String getEcsMetadataFileName() {
    String metadataFilename = System.getenv(ecs_metadata_file_env);
    if (metadataFilename == null || metadataFilename.equals("")) {
      return null;
    }

    return metadataFilename;
  }

  public static boolean isRunningInEcs() {
    return getEcsMetadataFileName() != null;
  }

  public static EcsMetadata getEcsMetadata() throws IOException {
    String metadataFileName = getEcsMetadataFileName();
    if (metadataFileName == null) {
      return null;
    }

    EcsMetadata metadata;
    try {
      metadata = objectMapper.readValue(metadataFileName, EcsMetadata.class);
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new IOException(e);
    }
    return metadata.getTaskDefinitionFamily() != null && metadata.getTaskDefinitionRevision() != null ? metadata : null;
  }

  public class EcsMetadata {

    @JsonProperty
    private String taskDefinitionFamily = null;

    @JsonProperty
    private String taskDefinitionRevision = null;

    public String getTaskDefinitionFamily() {
      return taskDefinitionFamily;
    }

    public String getTaskDefinitionRevision() {
      return taskDefinitionRevision;
    }
  }

}