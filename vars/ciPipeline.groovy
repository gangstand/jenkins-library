def call(Map config) {
    pipeline {
        agent any

        options {
            skipDefaultCheckout(true)
            timestamps()
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                when {
                    expression {
                        pipelineRules.shouldBuild()
                    }
                }

                steps {
                    script {
                        String tag = pipelineRules.isReleaseTag()
                            ? env.TAG_NAME
                            : env.GIT_COMMIT.take(8)

                        config.services.each { serviceName, service ->

                            String dockerfile = service.dockerfile ?: 'Dockerfile'

                            if (pipelineRules.isDevelopment() &&
                                service.developmentDockerfile) {
                                dockerfile = service.developmentDockerfile
                            }

                            if ((pipelineRules.isProduction() ||
                                 pipelineRules.isReleaseTag()) &&
                                service.productionDockerfile) {
                                dockerfile = service.productionDockerfile
                            }

                            String imageName = service.image ?: serviceName
                            String name = "${config.project}/${imageName}"

                            dockerBuild(
                                registry: config.registry,
                                name: name,
                                context: service.context,
                                dockerfile: dockerfile,
                                tag: tag
                            )
                        }
                    }
                }
            }

            stage('Release') {
                when {
                    expression {
                        pipelineRules.shouldBuild()
                    }
                }

                steps {
                    script {
                        String tag = pipelineRules.isReleaseTag()
                            ? env.TAG_NAME
                            : env.GIT_COMMIT.take(8)

                        String channel = pipelineRules.isDevelopment()
                            ? 'development'
                            : 'production'

                        config.services.each { serviceName, service ->

                            String imageName = service.image ?: serviceName
                            String name = "${config.project}/${imageName}"

                            dockerRelease(
                                registry: config.registry,
                                credentialsId: config.harborCredentials,
                                name: name,
                                image: "${config.registry}/${name}:${tag}",
                                channel: channel
                            )
                        }
                    }
                }
            }
        }

        post {
            always {
                deleteDir()
            }
        }
    }
}
