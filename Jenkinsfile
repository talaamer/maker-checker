pipeline {
    agent any

    parameters {
        choice(name: 'TARGET_ENV', choices: ['dev', 'prod'], description: 'Select deployment target')
    }

    environment {
        JAVA_HOME = 'C:\\Users\\D-12\\Downloads\\jdk-21.0.11'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
        IMAGE_NAME = 'maker-checker'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'gradlew.bat clean test bootJar'
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t ${IMAGE_NAME}:${params.TARGET_ENV} ."
            }
        }

        stage('Deploy') {
            steps {
                script {
                    if (params.TARGET_ENV == 'dev') {
                        echo 'Deploying to development environment'
                    } else {
                        echo 'Deploying to production environment'
                    }
                }
            }
        }
    }
}
