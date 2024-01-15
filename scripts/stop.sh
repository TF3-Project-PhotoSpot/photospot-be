  CONTAINER_NAME=photospot
  CONTAINER_ID=$(docker ps -a -q --filter "name=$CONTAINER_NAME")
  CONTAINER_IMAGE=$(docker inspect --format='{{.Config.Image}}' $CONTAINER_ID)

  cd /home/ubuntu/app

  docker-compose down

  if [ -n "$CONTAINER_ID" ]; then
    docker rmi $CONTAINER_IMAGE
  fi