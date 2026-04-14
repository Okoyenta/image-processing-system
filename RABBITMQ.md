## pulling the docker image for rabbitmq
docker pull rabbitmq:management-alpine

## running the docker image for rabbitmq
docker run -d  --name rabbitmq  -p 5672:5672  -p 15672:15672  rabbitmq:manement-alpineag

## accessing the management interface
Open a web browser and navigate to http://localhost:15672. Use the default credentials (username: guest, password: guest) to log in.

5672  - the springboot app connect to rabbitmq using this port
15672 - Web dashboard (monitor queues, messages)

docker stop rabbitmq
docker start rabbitmq

# add persistance storage to rabbitmq, so your queues and messages will not be lost when you stop the container
docker run -d  --name rabbitmq  -p 5672:5672  -p 15672:15672  -v rabbitmq_data:/var/lib/rabbitmq rabbitmq:management-alpine

# Set custom username and password for rabbitmq
docker run -d  --name rabbitmq  -p 5672:5672  -p 15672:15672  -v rabbitmq_data:/var/lib/rabbitmq -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin123 rabbitmq:management-alpine

