# Application Deployment Instructions

## Prerequisites
Ensure you have the following installed:
- Maven
- Docker

## Steps

### 1. Change to the Target Directory
Navigate to the target folder where your project is located.
\```bash
cd Accounts
\```

### 2. Build Maven Jar
Use the following Maven command to clean the project and build the jar file:
\```bash
mvn clean install
\```

### 3. Build the Docker Image
Compile your application and build the Docker image using Jib:
\```bash
mvn compile jib:dockerBuild
\```

### 4. Run the Docker Image
To run your Docker container and map the ports:
\```bash
docker run -p 8080:8080 thrilokh/accounts:v1
\```

### 5. Run the Image in Detached Mode
To run your Docker container in detached mode (no logs):
\```bash
docker run -d -p 8080:8080 thrilokh/accounts:v1
\```

### 6. Manage Docker Containers

#### View Running Containers
To see only running containers:
\```bash
docker ps
\```

#### View All Containers
To see all containers, including stopped ones:
\```bash
docker ps -a
\```

### 7. Exit a Running Container
To exit a running container, use:
\```bash
# Press control + c to exit
\```

## Additional Information

Ensure that you have configured Docker and Maven correctly on your system to avoid any issues during the build and run phases. If you encounter any errors, check the version compatibility of Maven and Docker with your project.


### To Run All Containers
To exit a running container, use:
\```bash
docker compose up *OR* docker compose up -d
docker compose down
\```