# Loan API

A demonstration of a simple Loan API which allows bank employees to create, 
list and pay loans for the bank customers.

## Running the application
To be able to build and run the application, you need to have at least Java 17 installed on your machine.

### Using Docker
To build and run the application using Docker, you first need to build the application using Maven and then build
the Docker image and start the containers using Docker Compose. In order to do that, run the following commands:
```bash
$ mvn clean package
$ docker-compose up
```

### Using Maven
To run the application using Maven, run the following command:
```bash
$ mvn spring-boot:run
```

After running the application using either of the methods above, the application will listen on port 8090.
You can access the API documentation by visiting [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html).

## Using the Application
### Authentication
The application uses Basic Authentication to authenticate users. Username and password of the pre-defined admin user
is `admin` and `password` respectively. You can authenticate as the admin user by providing the username and password.

### Pre-defined Customers
The application comes with 3 pre-defined customers. Customers and their initial credit limits
are as follows:
- Customer 1: ID 100001, Credit Limit 10.000.000
- Customer 2: ID 100002, Credit Limit 500.000
- Customer 3: ID 100003, Credit Limit 7.500.000

### Available Endpoints
The application provides the following endpoints:
- `GET /api/loans`: List all loans of a customer
- `GET /api/loans/{loanId}/installments`: List all installments of a loan
- `POST /api/loans`: Create a new loan for a customer
- `POST /api/loans/{loanId}/payments`: Pay a loan

For more information on the endpoints, you can visit the API documentation at [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html).

## Accessing the H2 Console
The application uses an in-memory H2 database. If you run the application with Spring profile `dev` (`mvn spring-boot:run -Dspring-boot.run.profiles=dev`), the H2 console will be available.
Then you can access the H2 console by visiting [http://localhost:8090/h2-console](http://localhost:8090/h2-console).
You can log in to the H2 console using the following credentials and browse the database:
- JDBC URL: `jdbc:h2:mem:loan-api`
- User Name: `sa`
- Password: `password`
