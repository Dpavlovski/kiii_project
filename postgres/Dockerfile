FROM postgres:latest

ENV POSTGRES_USER finki_admin
ENV POSTGRES_PASSWORD f1nk1_@dm1n
ENV POSTGRES_DB finki-services-db

COPY backup.sql /docker-entrypoint-initdb.d/
