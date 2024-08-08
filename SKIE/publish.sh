set -xe

./gradlew :initializeSonatypeStagingRepository

./gradlew findSonatypeStagingRepository -x initializeSonatypeStagingRepository publishToSonatype

./gradlew :findSonatypeStagingRepository -x initializeSonatypeStagingRepository :closeAndReleaseSonatypeStagingRepository
