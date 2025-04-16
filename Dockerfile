FROM openjdk:21-jdk

ARG MONGO_DATABASE_URL
ARG SENDGRID_API_KEY

WORKDIR /app

COPY target/todo-list-backend-0.0.1-SNAPSHOT.jar /app/todo-list-backend.jar

ENV MONGO_DATABASE_URL=${MONGO_DATABASE_URL}
ENV SENDGRID_API_KEY=${SENDGRID_API_KEY}

EXPOSE 8080

CMD ["java", "-jar", "todo-list-backend.jar"]