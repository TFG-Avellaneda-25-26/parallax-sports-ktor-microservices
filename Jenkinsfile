pipeline {
    agent any

    environment {
        REGISTRY    = 'localhost:5000'
        STACK_PATH  = '/opt/stack'
        // Listed once; reused for build/push/deploy loops.
        // ms-playwright uses Dockerfile.playwright; others share Dockerfile.
        SERVICES        = 'ms-discord ms-email ms-cloudinary'
        PLAYWRIGHT_SVC  = 'ms-playwright'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Decrypt secrets') {
            steps {
                // Same approach as Spring pipeline. The credential below is the
                // git-crypt symmetric key for THIS repo (different from the Spring
                // and infra keys — exported separately via `git-crypt export-key`).
                withCredentials([file(credentialsId: 'gitcrypt-ktor', variable: 'GC_KEY')]) {
                    sh '''
                        git-crypt unlock "$GC_KEY"
                        # Sanity check: one of the secrets files should now be plaintext HOCON.
                        head -1 ms-discord/src/main/resources/application-secrets.conf \
                            | grep -q '^\\(parallaxbot\\|#\\|include\\)' \
                            || (echo "Secrets did not decrypt to plain HOCON!" && exit 1)
                        echo "Secrets decrypted"
                    '''
                }
            }
        }

        stage('Build distributions') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean installDist --no-daemon -x test
                '''
            }
        }

        stage('Build & push images') {
            steps {
                script {
                    // 4 standard services share the reusable Dockerfile.
                    SERVICES.split().each { svc ->
                        sh """
                            docker build \
                                --build-arg SERVICE=${svc} \
                                -t ${REGISTRY}/parallax-${svc}:latest \
                                -t ${REGISTRY}/parallax-${svc}:${BUILD_NUMBER} \
                                .
                            docker push ${REGISTRY}/parallax-${svc}:latest
                            docker push ${REGISTRY}/parallax-${svc}:${BUILD_NUMBER}
                        """
                    }

                    // ms-playwright uses its own Dockerfile (browsers baked in).
                    sh """
                        docker build \
                            -f Dockerfile.playwright \
                            -t ${REGISTRY}/parallax-${PLAYWRIGHT_SVC}:latest \
                            -t ${REGISTRY}/parallax-${PLAYWRIGHT_SVC}:${BUILD_NUMBER} \
                            .
                        docker push ${REGISTRY}/parallax-${PLAYWRIGHT_SVC}:latest
                        docker push ${REGISTRY}/parallax-${PLAYWRIGHT_SVC}:${BUILD_NUMBER}
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def all = SERVICES.split().toList() + [PLAYWRIGHT_SVC]
                    def serviceList = all.join(' ')
                    sh """
                        cd ${STACK_PATH}
                        COMPOSE_PROFILES=apps docker compose pull ${serviceList}
                        COMPOSE_PROFILES=apps docker compose up -d --no-deps --force-recreate --pull=always ${serviceList}
                    """
                }
            }
        }
    }

    post {
        always {
            // Wipe decrypted secrets from workspace before cleanup.
            sh '''
                find . -name "application-secrets.conf" -path "*/main/resources/*" -delete || true
                find . -name "shared-secrets.conf"      -path "*/main/resources/*" -delete || true
            '''
            cleanWs()
        }
        success {
            echo "Deployed 5 Ktor microservices, build #${BUILD_NUMBER}"
        }
        failure {
            echo "Build #${BUILD_NUMBER} failed"
        }
    }
}
