pipeline {
    agent any

    environment {
        JAVA_HOME = 'C:\\Users\\D-12\\Downloads\\jdk-21.0.11'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
        IMAGE_NAME = 'maker-checker'
        IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code'
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew.bat clean test bootJar'
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Docker Run') {
            steps {
                bat "docker rm -f ${IMAGE_NAME} || exit 0"
                bat "docker run -d --name ${IMAGE_NAME} -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }
    }
}
