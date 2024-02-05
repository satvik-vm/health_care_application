# Introduction
This is the official repo for team 6 with track 6 for health care application development course for team 2 of acadmeic year 2023 - 2024.

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

### Planning on using
In the future we are planning on using the following technologies: -
1. React
2. React Native
3. Redis

# How to use
Clone the repo into your local system and follow the instructions
## Setting up the database
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
```
