#!/usr/bin/env bash

SHOWCASE_VERSION=0.0.4
SHOWCASE_RELEASE_URL=https://github.com/googleapis/gapic-showcase/releases/download/v${SHOWCASE_VERSION}
SHOWCASE_PROTO_ARTIFACT=${SHOWCASE_RELEASE_URL}/gapic-showcase-v1alpha1-${SHOWCASE_VERSION}-protos.tar.gz
SHOWCASE_SERVER_ARTIFACT=${SHOWCASE_RELEASE_URL}/gapic-showcase-v1alpha1-${SHOWCASE_VERSION}-darwin-amd64

GAPIC_GENERATOR="$(pwd)"
SHOWCASE_PROTOS="${GAPIC_GENERATOR}/showcase-protos"
REPORTS_DIR="${GAPIC_GENERATOR}/reports"
LANGUAGE=php

function run_showcase_server {
    echo "Starting showcase server..."
    curl -sL ${SHOWCASE_SERVER_ARTIFACT} --output showcase-server
    chmod a+x showcase-server
    ./showcase-server > ${REPORTS_DIR}/showcase_server.log 2>&1 &
}

function generate_showcase_libs {
    echo "Generating showcase libs..."
    mkdir -p ${REPORTS_DIR}
    rm -rf ${SHOWCASE_PROTOS}
    curl -sL ${SHOWCASE_PROTO_ARTIFACT} | tar xz
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
}

#generate_showcase_libs
run_showcase_server

cd showcase/php
composer update
vendor/bin/phpunit tests/ShowcaseIntegrationTests.php
