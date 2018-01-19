/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.transformer.php;

import com.google.api.codegen.TargetLanguage;
import com.google.api.codegen.config.ApiModel;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.transformer.PackageMetadataNamer;
import com.google.api.codegen.transformer.PackageMetadataTransformer;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.viewmodel.ApiMethodView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.codegen.viewmodel.metadata.PackageDependencyView;
import com.google.api.codegen.viewmodel.metadata.ReadmeMetadataView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Responsible for producing package metadata related views for PHP */
public class PhpPackageMetadataTransformer implements ModelToViewTransformer {
  private static final Map<String, String> TOP_LEVEL_TEMPLATE_FILES =
      ImmutableMap.<String, String>builder()
          .put("LICENSE.snip", "LICENSE")
          .put("php/bootstrap_unit.snip", "tests/unit/bootstrap.php")
          .put("php/bootstrap_system.snip", "tests/system/bootstrap.php")
          .put("php/composer.snip", "composer.json")
          .put("php/CONTRIBUTING.snip", "CONTRIBUTING.md")
          .put("php/phpunit.snip", "phpunit.dist.xml")
          .put("php/phpunit-system.snip", "phpunit-system.dist.xml")
          .put("php/README.snip", "README.md")
          .put("php/VERSION.snip", "VERSION")
          .build();

  private PackageMetadataConfig packageConfig;
  private PackageMetadataTransformer metadataTransformer = new PackageMetadataTransformer();

  public PhpPackageMetadataTransformer(PackageMetadataConfig packageConfig) {
    this.packageConfig = packageConfig;
  }

  @Override
  public List<String> getTemplateFileNames() {
    List<String> templates = new ArrayList<>();
    templates.addAll(TOP_LEVEL_TEMPLATE_FILES.keySet());
    return templates;
  }

  @Override
  public List<ViewModel> transform(ApiModel model, GapicProductConfig productConfig) {
    List<ViewModel> models = new ArrayList<>();
    PhpPackageMetadataNamer metadataNamer =
        new PhpPackageMetadataNamer(
            productConfig.getPackageName(), productConfig.getDomainLayerLocation());
    SurfaceNamer surfaceNamer = new PhpSurfaceNamer(productConfig.getPackageName());
    for (Map.Entry<String, String> entry : TOP_LEVEL_TEMPLATE_FILES.entrySet()) {
      models.add(
          generateMetadataView(
              model, productConfig, metadataNamer, surfaceNamer, entry.getKey(), entry.getValue()));
    }
    return models;
  }

  private ViewModel generateMetadataView(
      ApiModel model,
      GapicProductConfig productConfig,
      PackageMetadataNamer metadataNamer,
      SurfaceNamer surfaceNamer,
      String template,
      String outputPath) {
    List<PackageDependencyView> dependencies =
        ImmutableList.of(
            PackageDependencyView.create(
                "google/gax", packageConfig.gaxVersionBound(TargetLanguage.PHP)),
            PackageDependencyView.create(
                "google/protobuf", packageConfig.protoVersionBound(TargetLanguage.PHP)));

    String gapicPackageName =
        surfaceNamer.getGapicPackageName(packageConfig.packageName(TargetLanguage.PHP));
    return metadataTransformer
        .generateMetadataView(
            metadataNamer, packageConfig, model, template, outputPath, TargetLanguage.PHP)
        .additionalDependencies(dependencies)
        .hasMultipleServices(model.hasMultipleServices())
        .identifier(metadataNamer.getMetadataIdentifier())
        .readmeMetadata(
            ReadmeMetadataView.newBuilder()
                .moduleName("")
                .shortName(packageConfig.shortName())
                .fullName(model.getTitle())
                .apiSummary(model.getDocumentationSummary())
                .hasMultipleServices(false)
                .gapicPackageName(gapicPackageName)
                .majorVersion(packageConfig.apiVersion())
                .developmentStatusTitle(
                    metadataNamer.getReleaseAnnotation(
                        metadataTransformer.getMergedReleaseLevel(
                            packageConfig, productConfig, TargetLanguage.PHP)))
                .targetLanguage("PHP")
                .mainReadmeLink("")
                .libraryDocumentationLink("")
                .authDocumentationLink("")
                .versioningDocumentationLink("")
                .exampleMethods(ImmutableList.<ApiMethodView>of())
                .build())
        .build();
  }
}
