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
      this.metadataIdenfifier =
          PhpPackageUtil.formatComposerPackageName(domainLayerLocation, packageName);
    } else {
      // If no domainLayerLocation is provided, take the first component of the packageName and use
      // that as the vendor.
      String normalizedPackageName =
          StringUtil.removePrefix(packageName, PhpPackageUtil.PACKAGE_SEPARATOR);

      int firstSeparatorIndex = normalizedPackageName.indexOf(PhpPackageUtil.PACKAGE_SEPARATOR);

      if (firstSeparatorIndex == -1) {
        this.metadataIdenfifier =
            PhpPackageUtil.formatComposerPackageName(normalizedPackageName, normalizedPackageName);
      } else {
        String vendor = normalizedPackageName.substring(0, firstSeparatorIndex);
        String project =
            normalizedPackageName.substring(
                firstSeparatorIndex + PhpPackageUtil.PACKAGE_SEPARATOR.length());
        this.metadataIdenfifier = PhpPackageUtil.formatComposerPackageName(vendor, project);
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
