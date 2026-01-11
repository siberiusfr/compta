package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "Request for setting document metadata")
public class MetadataRequest {

  @NotEmpty(message = "Metadata must not be empty")
  @Schema(
      description = "Key-value pairs of metadata",
      example = "{\"author\": \"John Doe\", \"department\": \"Finance\"}")
  private Map<String, String> metadata;
}
