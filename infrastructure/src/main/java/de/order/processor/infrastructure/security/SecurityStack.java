package de.order.processor.infrastructure.security;

import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.kms.Key;
import software.amazon.awscdk.services.kms.KeyProps;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static de.order.processor.infrastructure.configuration.Configuration.STAGE;

public class SecurityStack extends NestedStack {

  private final IKey applicationKey;

  public SecurityStack(Construct scope, String id) {
    this(scope, id, null);
  }

  public SecurityStack(Construct scope, String id, NestedStackProps props) {
    super(scope, id, props);

    var policy = new PolicyDocument(PolicyDocumentProps.builder()
        .statements(List.of(
            new PolicyStatement(PolicyStatementProps.builder()
                .sid("Enable IAM User Permissions")
                .effect(Effect.ALLOW)
                .principals(List.of(new AccountRootPrincipal()))
                .actions(List.of(
                    "kms:*"
                ))
                .resources(List.of("*"))
                .build()),
            new PolicyStatement(PolicyStatementProps.builder()
                .sid("Enable LogGroup encryption")
                .effect(Effect.ALLOW)
                .principals(List.of(new ServicePrincipal("logs." + getRegion() + ".amazonaws.com")))
                .actions(List.of(
                    "kms:Encrypt*",
                    "kms:Decrypt*",
                    "kms:ReEncrypt*",
                    "kms:GenerateDataKey*",
                    "kms:Describe*"
                ))
                .resources(List.of("*"))
                .conditions(Map.of(
                    "ArnLike", Map.of(
                        "kms:EncryptionContext:aws:logs:arn", "arn:aws:logs:" + getRegion() + ":" + getAccount() + ":*"
                    )
                ))
                .build()))
        )
        .build());
    applicationKey = new Key(
        this,
        "ApplicationKey" + STAGE.toUpperCase(),
        KeyProps.builder()
            .description("Application key for the OrderProcessor App")
            .enableKeyRotation(true)
            .policy(policy)
            .build());
    applicationKey.addAlias("OrderProcessorApplicationKey" + STAGE.toUpperCase());
  }

  public IKey getApplicationKey() {
    return applicationKey;
  }

}
