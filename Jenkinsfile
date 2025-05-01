pipeline {
    agent any
    environment {
	    SERVICE_REPO_NAME = 'RTM_AuthService.git'
	    DOCKER_IMAGE = 'hyundooboo/rtm-auth-service'
	    SERVICE_NAME = 'auth-service'
	    SPRING_DATASOURCE_URL = credentials('postgres-url')
	    DB_CREDENTIALS = credentials('postgres-db-credentials')
	    GITHUB_CREDENTIALS = credentials('github-token')
	    HELM_REPO_URL = 'https://github.com/Ready-to-Marry/RTM_infra.git'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "https://github.com/Ready-to-Marry/${SERVICE_REPO_NAME}", credentialsId: 'github-token'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    echo "🔨 Building Docker image: ${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                    docker.build("${DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        docker.withRegistry('', 'dockerhub') {
                            echo "📤 Pushing image to Docker Hub..."
                            docker.image("${DOCKER_IMAGE}:${env.BUILD_NUMBER}").push()
                        }
                    }
                }
            }
        }
        stage('Update Helm values.yaml') {
            steps {
                script {
                    echo "🛠️ Cloning Helm chart repo..."
                    sh 'rm -rf rtm-helm'

	            withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
	                sh "git clone https://${GITHUB_USERNAME}:${GITHUB_TOKEN}@github.com/Ready-to-Marry/RTM_infra.git rtm-helm"
	
	                echo "🔧 Updating image tag to ${env.BUILD_NUMBER}..."
	                sh """
	                  sed -i 's/^  tag: .*/  tag: "${env.BUILD_NUMBER}"/' rtm-helm/${SERVICE_NAME}/values.yaml
	                """
	
	                dir('rtm-helm') {
	                    sh 'git config user.name "hyundoo"'
	                    sh 'git config user.email "hyundoo1006@gmail.com"'
	                    
	                    sh """
	                    git commit -am 'Update image tag to ${env.BUILD_NUMBER}'
	                    git push origin HEAD:main
	                    """
	                }
	            }
                }
            }
        }
    }
}
