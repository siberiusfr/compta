package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for uploading a new document version")
public class DocumentVersionUploadRequest {

  @Size(max = 500, message = "Change description must not exceed 500 characters")
  @Schema(description = "Description of changes in this version", example = "Fixed typos and updated figures")
  private String changeDescription;
}
