#!/bin/bash

# EC2 정보 설정
EC2_USER="ubuntu"
EC2_HOST="13.125.133.147"
PEM_PATH="~/.ssh/hive.pem"

# 로컬에서 빌드 및 Docker 이미지 푸시
echo "=======step[1] : Building project..."
./gradlew build -x test

echo "=======step[2] :Building Docker image..."
docker build -f Dockerfile.prod -t mical150/rcall .

echo "=======step[3] :Pushing Docker image to registry..."
docker push mical150/rcall:latest

echo "Deployment to Docker registry complete!"

# EC2에 SSH로 접속하여 Docker 명령어 실행
echo "=======step[4] :Deploying to EC2..."

ssh -i "$PEM_PATH" "$EC2_USER@$EC2_HOST" << EOF
  echo "==========step[5]  :Stopping Docker containers on EC2..."
  docker compose down

  echo "===========step[6]  :Removing old Docker image..."
  docker rmi mical150/rcall:latest

  echo "===========step[7]  :Starting Docker containers..."
  docker compose up -d

  echo "Deployment on EC2 complete!"
EOF

echo "Deployment complete!"
