# Spring Framework 6: Beginner to Guru

## spring-6-resttemplate

This project acts as a client to the spring-6-rest-mvc which runs on port 8086.
Required other modules up and running:
- this application runs on port 8086 
- authentication server on port 9000
- mvc module running on port 8081

Website: http://localhost:8086/beers

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
