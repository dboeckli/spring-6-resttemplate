# Spring Framework 6: Beginner to Guru

## spring-6-resttemplate

This project acts as a client to the spring-6-resttemplate which runs on port 8086.
Required other modules up and running:
- this application runs on port 8086/30086 
- authentication server on port 9000/30900
- mvc module running on port 8081/30081
- gateway module running on port 8080/30080

## Web Interface

This application includes a web interface that allows users to interact with the beer data through a browser. The web interface provides the following features:

- View a paginated list of beers
- Navigate through pages of beer listings
- View details of individual beers

To access the web interface, start the application and navigate to: 
- http://localhost:8086/beers
- http://localhost:30086/beers

To access the openapi ui from the mvc server:
- http://localhost:8081/swagger-ui/index.html
- http://localhost:30081/swagger-ui/index.html

## Overview
All components are started automatically with the help of docker-compose.

Without Gateway
```plaintext
+--------------+               +--------------------+
| Client       |               | Authentication     |
| (makes       |  -----------> | Server (Port 9000) |
| request)     |  <----------- | (returns token)    |
+--------------+               +--------------------+
     |   ^  
     |   |
     v   |
 +----------------+               
 | MVC Backend    |
 | (Port 8081)    |
 | (Executes      |
 | query and      |
 | creates        |
 | response)      |
 +----------------+
```
With Gateway
```plaintext
+---------+               +----------------+               +--------------------+
| Client  |               | Gateway Server |               | Authentication     |
| (makes  |  -----------> | (Port 8080)    |  -----------> | Server (Port 9000) |
| request)|  <----------- |                |  <----------- | (returns token)    |
+---------+               +----------------+               +--------------------+
                                |   ^  
                                |   |
                                v   |
                           +----------------+               
                           | MVC Backend    |
                           | (Port 8081)    |
                           | (Executes      |
                           | query and      |
                           | creates        |
                           | response)      |
                           +----------------+
```
The Integration Test only covers the scenario without gateway. See project spring-6-restclient for integration test with gateway.

## Kubernetes

### Generate Config Map for mysql init script

When updating 'src/scripts/init-mysql-mysql.sql', apply the changes to the Kubernetes ConfigMap:
```bash
kubectl create configmap mysql-init-script --from-file=init.sql=src/scripts/init-mysql.sql --dry-run=client -o yaml | Out-File -Encoding utf8 k8s/mysql-init-script-configmap.yaml
```

To run maven filtering for destination target/k8s and target target/helm run:
```bash
mvn clean install -DskipTests 
```

### Deployment with Kubernetes

Deployment goes into the default namespace.

To deploy all resources:
```bash
kubectl apply -f target/k8s/
```

To remove all resources:
```bash
kubectl delete -f target/k8s/
```

Check
```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

You can use the actuator rest call to verify via port 30086

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter spring-6-resttemplate-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace spring-6-resttemplate --create-namespace --wait --timeout 5m --debug
```

show logs and show event
```powershell
kubectl get pods -n spring-6-resttemplate
```
replace $POD with pods from the command above
```powershell
kubectl logs $POD -n spring-6-resttemplate --all-containers
```

Show Details and Event

$POD_NAME can be: spring-6-resttemplate-mongodb, spring-6-resttemplate
```powershell
kubectl describe pod $POD_NAME -n spring-6-resttemplate
```

Show Endpoints
```powershell
kubectl get endpoints -n spring-6-resttemplate
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME --namespace spring-6-resttemplate
```

You can use the actuator rest call to verify via port 30086
