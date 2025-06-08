package de.order.processor.infrastructure.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitHubRepository {
  private String owner;
  private String repository;
  private String branch;

  public String fullRepositoryPath() {
    return owner + "/" + repository;
  }
}