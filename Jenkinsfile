pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        BACKEND_IMAGE = 'sophatel-backend:local'
        FRONTEND_IMAGE = 'sophatel-front-end:local'
        COMPOSE_PROJECT_NAME = 'stage-ci'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend tests') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B clean test'
                }
            }
        }

        stage('Frontend build') {
            steps {
                dir('front-end') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker build') {
            steps {
                sh 'docker build -t "$BACKEND_IMAGE" backend'
                sh 'docker build -t "$FRONTEND_IMAGE" front-end'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
        }
    }
}
