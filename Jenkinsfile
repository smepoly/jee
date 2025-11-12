// Jenkins pipeline pour construire, scanner (Trivy) et publier les images Docker

// (La liste des microservices est définie dans le bloc environment ci-dessous)

// Pipeline déclarative
pipeline {
    agent any

    parameters {
        string(name: 'DOCKER_NAMESPACE', defaultValue: 'sabri74155', description: 'Namespace Docker Hub')
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Tag des images (par défaut: BUILD_NUMBER)')
    }

    environment {
        // Si IMAGE_TAG vide, utiliser le numéro de build Jenkins
        TAG = "${params.IMAGE_TAG != '' ? params.IMAGE_TAG : env.BUILD_NUMBER}"
        // Liste des microservices à builder (séparés par des espaces)
        SERVICES = "api-gateway appointment-service doctor-service eureka-server patient-service"
    }

    stages {
        stage('Checkout') {
            when {
                expression { !fileExists('api-gateway/pom.xml') }
            }
            steps {
                script {
                    echo 'Checking out repository sources...'
                    git branch: 'main', url: 'https://github.com/smepoly/jee.git'
                }
            }
        }

        stage('Build JARs (Maven)') {
            steps {
                script {
                    env.SERVICES.tokenize().each { service ->
                        dir(service) {
                            echo "Building ${service}..."
                            sh '''
                                set -e
                                mkdir -p .mvn/wrapper
                                if [ ! -f .mvn/wrapper/maven-wrapper.properties ]; then
                                  cat > .mvn/wrapper/maven-wrapper.properties <<'EOF'
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
EOF
                                fi
                                chmod +x mvnw || true
                                ./mvnw -B -DskipTests clean package
                            '''
                        }
                    }
                }
            }
        }

        stage('Build images') {
            steps {
                script {
                    env.SERVICES.tokenize().each { service ->
                        echo "Building ${params.DOCKER_NAMESPACE}/${service}:${TAG}"
                        sh "docker build -t ${params.DOCKER_NAMESPACE}/${service}:${TAG} ./${service}"
                    }
                }
            }
        }

        stage('Scan (Trivy)') {
            steps {
                script {
                    // Continue même si le scan échoue (warning uniquement)
                    def isFirstScan = true
                    env.SERVICES.tokenize().each { service ->
                        echo "Scanning ${params.DOCKER_NAMESPACE}/${service}:${TAG}"
                        try {
                            // Pour le premier scan, télécharger les DBs; pour les suivants, les réutiliser
                            def skipDbUpdate = isFirstScan ? "" : "--skip-java-db-update --skip-db-update"
                            isFirstScan = false
                            
                            sh """
                                docker run --rm \
                                  -v /var/run/docker.sock:/var/run/docker.sock \
                                  -v trivy-cache:/root/.cache/ \
                                  aquasec/trivy:latest image \
                                  --timeout 30m \
                                  --exit-code 0 \
                                  --severity HIGH,CRITICAL \
                                  --no-progress \
                                  --scanners vuln \
                                  ${skipDbUpdate} \
                                  ${params.DOCKER_NAMESPACE}/${service}:${TAG}
                            """
                        } catch (Exception e) {
                            echo " Trivy scan failed for ${service}, but continuing pipeline..."
                            echo "Error: ${e.message}"
                            // Ne pas faire échouer le build, juste warning
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'

                        env.SERVICES.tokenize().each { service ->
                            echo "Pushing ${params.DOCKER_NAMESPACE}/${service}:${TAG}"
                            sh "docker push ${params.DOCKER_NAMESPACE}/${service}:${TAG}"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline terminé.'
        }
        success {
            echo ' Build, scan et push réussis!'
        }
        unstable {
            echo ' Build et push réussis, mais le scan Trivy a trouvé des problèmes.'
        }
        failure {
            echo ' Pipeline échoué.'
        }
    }
}
