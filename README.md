# Introduction
* This is the official repo for team 6 with track 6 for health care application development course for team 2 of acadmeic year 2023 - 2024.
* This branch deals with the implementation of kubernetes and distributed architecture for the project.


## Collaborators
The following are the collaborators for this project: -
1. [Satvik Verma](https://www.linkedin.com/in/satvik-vm/)
2. [Mukul Gupta](https://www.linkedin.com/in/mukul-gupta-995397211/)
3. [Kalyani Verma](https://www.linkedin.com/in/kalyaniverma/)
4. [Manas Agrawal](https://www.linkedin.com/in/manas-agrawal-1bb087202/)
5. [Aryan Bhatt](https://www.linkedin.com/in/aryan-bhatt-ba4a04200/)

## Tech Stack
### Used
We have used the following technologies till now: -
1. Postgres
2. Spring boot with maven
3. Prisma
4. Kubernetes
5. React

### Planning on using
In the future we are planning on using the following technologies: -
1. React Native
2. Redis

# How to use
Clone the repo into your local system and follow the instructions
<!-- ## Setting up the database
1. Start a postgres server.
2. Create a database on the psql server.
3. Create a .env file inside the prisma directory.
4. Put your postgres database url in the e.nv file in the following format : -
```console
DATABASE_URL="postgres://<username>:<password>@localhost:5432/<db>?schema=public"
```
5. Go into the prisma directory and run the following command : -
```console
$ npx prisma migrate dev --name init
```
This will setup your database.

## Starting spring boot server
1. Make sure your postgres server is running
2. Go to demo directory.
3. Run the following command : -
```console
$ mvn clean install
```
4. This will create the jar file inside the target directory.
5. Start the jar by the following command : -
```console
java -jar target/demo-0.0.1-SNAPSHOT.jar
``` -->

1. Switch to branch dev_k8s.
2. cd into kubernetes directory from home directory using the command
```console
$ cd server/demo/kubernetes
```
3. Install minikube following this [link](https://minikube.sigs.k8s.io/docs/start/)
4. Start the kubernetes cluster by using minikube command: -
```console
$ minikube start
```
5. Apply all config files from the config folder using the command
```console
$ kubectl apply -f config/<filename.yaml>
```
6. Apply all secret files from the secrets folder using the command
```console
$ kubectl apply -f secrets/<filename.yaml>
```
7. Apply the stateful set of postgres by the command
```console
$ kubectl apply -f statefulSet/postgres.yaml
```
8. Apply the deployment of server by the command
```console
$ kubectl apply -f deployment/server.yaml
```

This will start the backend server and the database server in two different pods that can communicate with each other.

