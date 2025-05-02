#!/usr/bin/env sh
#######################################################################################
# Copyright © 2024 Kaleyra S.p.a All Rights Reserved
# See LICENSE.txt for licensing information
#######################################################################################

usage() {
  echo "
  Usage: update_hybrid_bridge_github.sh version github-pem github-app-id github-install-id

  Triggers the github CI pipeline on hybrid bridge plugin

  Parameters:
  - github-token: The github token to be used to trigger the hybrid workflow dispatch
  - version: a string representing the version of the native android platform
  - release type: a string representing the release type that is going to be triggered. Can be 'patch' or 'minor'
  "
}

test ! -z "${1}" || { echo "Github token argument missing"; usage; exit 1; }
test ! -z "${2}" || { echo "Version argument missing"; usage; exit 1; }
test ! -z "${3}" || { echo "Release type argument missing"; usage; exit 1; }

echo "Triggering release hybrid bridge for version: ${2} ${3}"

curl --location 'https://api.github.com/repos/KaleyraVideo/VideoHybridNativeBridge/actions/workflows/update_native_dep.yml/dispatches' \
--header 'Accept: application/vnd.github+json' \
--header "Authorization: Bearer ${1}" \
--header 'X-GitHub-Api-Version: 2022-11-28' \
--header 'Content-Type: application/json' \
--data '{
    "ref": "main",
    "inputs": {
        "release_version": "'${2}'",
        "release_type": "'${3}'",
        "platform": "android"
    }
}'