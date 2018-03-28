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

import com.google.api.codegen.transformer.PackageMetadataNamer;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.StringUtil;
import com.google.api.codegen.util.php.PhpPackageUtil;
import java.util.Arrays;
import java.util.List;

/** PHPPackageMetadataNamer provides PHP specific names for metadata views. */
public class PhpPackageMetadataNamer extends PackageMetadataNamer {
  private Name serviceName;
  private String metadataIdenfifier;

  public PhpPackageMetadataNamer(String packageName, String domainLayerLocation) {
    // Get the service name from the package name by removing the version suffix (if any).
    this.serviceName = getApiNameFromPackageName(packageName);

    if (domainLayerLocation != null && !domainLayerLocation.equals("")) {
      // If a domainLayerLocation is provided, set the metadataIdentifier to be
      // "domainLayerLocation/serviceName".
      this.metadataIdenfifier = domainLayerLocation + "/" + serviceName.toSeparatedString("");
    } else {
      // If no domainLayerLocation is provided, take the first component of the packageName and use
      // that as the domain.
      String packageNameWithoutPrefix =
          StringUtil.removePrefix(packageName, PhpPackageUtil.PACKAGE_SEPARATOR);
      List<String> packageComponents =
          Arrays.asList(PhpPackageUtil.splitPackageName(packageNameWithoutPrefix));
      String domain = Name.upperCamel(packageComponents.get(0)).toSeparatedString("");
      if (packageComponents.size() == 1) {
        this.metadataIdenfifier = domain + "/" + domain;
      } else {
        String packageNameWithoutFirstComponent =
            PhpPackageUtil.buildPackageName(packageComponents.subList(1, packageComponents.size()));
        Name serviceNameWithoutFirstComponent =
            getApiNameFromPackageName(packageNameWithoutFirstComponent);
        this.metadataIdenfifier =
            domain + "/" + serviceNameWithoutFirstComponent.toSeparatedString("");
      }
    }
  }

  @Override
  public String getMetadataName() {
    return serviceName.toUpperCamel();
  }

  @Override
  public String getMetadataIdentifier() {
    return metadataIdenfifier;
  }

  public static Name getApiNameFromPackageName(String packageName) {
    return Name.upperCamel(
        PhpPackageUtil.splitPackageName(PhpPackageUtil.getPackageNameBeforeVersion(packageName)));
  }
}
