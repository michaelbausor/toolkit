#!/usr/bin/env bash

SHOWCASE_VERSION=0.0.4
GAPIC_GENERATOR="$(pwd)"
SHOWCASE_PROTOS="${GAPIC_GENERATOR}/showcase-protos"
REPORTS_DIR="${GAPIC_GENERATOR}/reports"
LANGUAGE=php

mkdir -p ${REPORTS_DIR}
rm -rf ${SHOWCASE_PROTOS}
curl -sL https://github.com/googleapis/gapic-showcase/releases/download/v${SHOWCASE_VERSION}/gapic-showcase-v1alpha1-${SHOWCASE_VERSION}-protos.tar.gz | tar xz
mv gapic-showcase-v1alpha1-${SHOWCASE_VERSION}-protos ${SHOWCASE_PROTOS}
TEST_SRC="${GAPIC_GENERATOR}/src/test/java/com/google/api/codegen/testsrc"
cp ${TEST_SRC}/showcase.yaml ${SHOWCASE_PROTOS}/google/showcase/
cp ${TEST_SRC}/showcase_gapic.yaml ${SHOWCASE_PROTOS}/google/showcase/v1alpha1/
cp ${TEST_SRC}/artman_showcase.yaml ${SHOWCASE_PROTOS}/google/showcase/

cd ${GAPIC_GENERATOR}
python generate_clients.py google/showcase/artman_showcase.yaml \
                --user-config=${GAPIC_GENERATOR}/.circleci/artman_config_local.yaml \
                --log=${REPORTS_DIR}/smoketest.log \
                --languages=${LANGUAGE} \
                --root-dir=${SHOWCASE_PROTOS}

