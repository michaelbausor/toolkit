/* Copyright 2016 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.transformer.php;

import com.google.api.HttpRule;
import com.google.api.codegen.InterfaceView;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.gapic.GapicCodePathMapper;
import com.google.api.codegen.transformer.FileHeaderTransformer;
import com.google.api.codegen.transformer.GapicInterfaceContext;
import com.google.api.codegen.transformer.GapicMethodContext;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.util.php.PhpTypeTable;
import com.google.api.codegen.viewmodel.HttpOptionView;
import com.google.api.codegen.viewmodel.HttpStubMethodView;
import com.google.api.codegen.viewmodel.HttpStubView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.Model;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Responsible for producing package metadata related views for PHP */
public class PhpPackageHttpStubTransformer implements ModelToViewTransformer {
  private static final String PACKAGE_FILE = "php/httpstub.snip";

  private GapicCodePathMapper pathMapper;
  private final FileHeaderTransformer fileHeaderTransformer =
      new FileHeaderTransformer(new PhpImportSectionTransformer());

  public PhpPackageHttpStubTransformer(GapicCodePathMapper pathMapper) {
    this.pathMapper = pathMapper;
  }

  @Override
  public List<String> getTemplateFileNames() {
    return Arrays.asList(PACKAGE_FILE);
  }

  @Override
  public List<ViewModel> transform(Model model, GapicProductConfig productConfig) {
    List<ViewModel> views = new ArrayList<>();
    Iterable<Interface> apiInterfaces = new InterfaceView().getElementIterable(model);
    for (Interface apiInterface : apiInterfaces) {
      ModelTypeTable modelTypeTable =
          new ModelTypeTable(
              new PhpTypeTable(productConfig.getPackageName()),
              new PhpModelTypeNameConverter(productConfig.getPackageName()));
      GapicInterfaceContext context =
          GapicInterfaceContext.create(
              apiInterface,
              productConfig,
              modelTypeTable,
              new PhpSurfaceNamer(productConfig.getPackageName()),
              new PhpFeatureConfig());
      views.add(generateHttpStubView(context));
    }
    return views;
  }

  private ViewModel generateHttpStubView(GapicInterfaceContext context) {
    SurfaceNamer namer = context.getNamer();
    String outputPath =
        pathMapper.getOutputPath(context.getInterface(), context.getProductConfig());
    String name = namer.getHttpStubClassName(context.getInterfaceConfig());
    addHttpStubImports(context);
    return HttpStubView.newBuilder()
        .templateFileName(PACKAGE_FILE)
        .outputPath(outputPath + "/" + name + ".php")
        .name(name)
        .apiMethods(generateHttpStubMethodViews(context))
        .fileHeader(fileHeaderTransformer.generateFileHeader(context))
        .build();
  }

  private void addHttpStubImports(GapicInterfaceContext context) {
    ModelTypeTable typeTable = context.getModelTypeTable();
    typeTable.saveNicknameFor("\\Google\\Cloud\\Core\\RequestWrapper");
    typeTable.saveNicknameFor("\\Google\\Cloud\\Core\\RestTrait");
    typeTable.saveNicknameFor("\\Google\\Cloud\\Core\\UriTrait");
    typeTable.saveNicknameFor("\\GuzzleHttp\\Psr7\\Request");
    typeTable.saveNicknameFor("\\Google\\GAX\\PathTemplate");
  }

  private List<HttpStubMethodView> generateHttpStubMethodViews(GapicInterfaceContext context) {
    List<HttpStubMethodView> httpStubMethods =
        new ArrayList<>(context.getInterface().getMethods().size());

    for (Method method : context.getSupportedMethods()) {
      httpStubMethods.add(generateHttpStubMethodView(context.asDynamicMethodContext(method)));
    }

    return httpStubMethods;
  }

  private HttpStubMethodView generateHttpStubMethodView(GapicMethodContext context) {
    SurfaceNamer namer = context.getNamer();
    return HttpStubMethodView.newBuilder()
        .name(namer.getHttpStubMethodName(context.getMethod()))
        .requestTypeName(
            namer.getRequestTypeName(context.getTypeTable(), context.getMethod().getInputType()))
        .responseTypeName(
            namer.getRequestTypeName(context.getTypeTable(), context.getMethod().getOutputType()))
        .httpOptions(generateHttpOptionViews(context))
        .build();
  }

  private List<HttpOptionView> generateHttpOptionViews(GapicMethodContext context) {
    List<HttpOptionView> optionViews = new ArrayList<>();

    for (FieldDescriptor fieldDescriptor : context.getMethod().getOptionFields().keySet()) {
      if (fieldDescriptor.getFullName().equals("google.api.http")) {
        HttpRule optionValue =
            (HttpRule) context.getMethod().getOptionFields().get(fieldDescriptor);

        addBindingOptionView(optionViews, optionValue);
        checkAndAddOptionView(optionViews, "body", optionValue.getBody());
        for (HttpRule additionalBindings : optionValue.getAdditionalBindingsList()) {
          //addBindingOptionView(optionViews, optionValue);
          // TODO: support additional bindings
        }
      }
    }

    return optionViews;
  }

  private void addBindingOptionView(List<HttpOptionView> optionViews, HttpRule optionValue) {
    checkAndAddOptionView(optionViews, "get", optionValue.getGet());
    checkAndAddOptionView(optionViews, "put", optionValue.getPut());
    checkAndAddOptionView(optionViews, "post", optionValue.getPost());
    checkAndAddOptionView(optionViews, "delete", optionValue.getDelete());
    checkAndAddOptionView(optionViews, "patch", optionValue.getPatch());
    // TODO: support custom bindings
  }

  private void checkAndAddOptionView(List<HttpOptionView> optionViews, String key, String value) {
    if (!value.isEmpty()) {
      optionViews.add(HttpOptionView.newBuilder().key(key).value(value).build());
    }
  }
}
