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
import java.util.List;

/** PHPPackageMetadataNamer provides PHP specific names for metadata views. */
public class PhpPackageMetadataNamer extends PackageMetadataNamer {
  private Name serviceName;
  private String metadataIdenfifier;

  public PhpPackageMetadataNamer(String packageName, String domainLayerLocation) {
    // Get the service name from the package name by removing the version suffix (if any).
    this.serviceName = getApiNameFromPackageName(packageName);

    Name normalizedPackageName = normalizePackage(packageName);

    if (domainLayerLocation != null && !domainLayerLocation.equals("")) {
      // If a domainLayerLocation is provided, set the metadataIdentifier to be
      // "domainLayerLocation/serviceName".
      this.metadataIdenfifier =
          PhpPackageUtil.formatComposerPackageName(
              Name.from(domainLayerLocation), normalizedPackageName);
    } else {
      // If no domainLayerLocation is provided, take the first component of the packageName and use
      // that as the vendor.

      List<Name> pieces = normalizedPackageName.toPieces();
      Name domain = pieces.get(0);

      if (pieces.size() == 1) {
        this.metadataIdenfifier = PhpPackageUtil.formatComposerPackageName(domain, domain);
      } else {
        Name trimmedPackageName = Name.from();
        for (Name piece : pieces.subList(1, pieces.size())) {
          trimmedPackageName = trimmedPackageName.join(piece);
        }
        this.metadataIdenfifier =
            PhpPackageUtil.formatComposerPackageName(domain, trimmedPackageName);
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

  /**
   * Normalize the package name by: - Removing the version - Splitting on PACKAGE_SEPARATOR and
   * converting each piece to lowercase unseparated string
   *
   * <p>So "Some\\PackageName\\V1" becomes Name["some", "packagename"];
   */
  private static Name normalizePackage(String packageName) {
    String packageNameWithoutVersion = PhpPackageUtil.getPackageNameBeforeVersion(packageName);
    String normalizedPackageName =
        StringUtil.removePrefix(packageNameWithoutVersion, PhpPackageUtil.PACKAGE_SEPARATOR);
    Name name = Name.from();
    for (String piece : PhpPackageUtil.splitPackageName(normalizedPackageName)) {
      name = name.join(Name.upperCamel(piece).toSeparatedString(""));
    }
    return name;
  }
}
