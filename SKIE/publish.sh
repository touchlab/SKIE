set -xe

export RELEASE_VERSION=0.9.0

./gradlew :initializeSonatypeStagingRepository

./gradlew findSonatypeStagingRepository -x initializeSonatypeStagingRepository publishToSonatype

./gradlew :findSonatypeStagingRepository -x initializeSonatypeStagingRepository :closeSonatypeStagingRepository
