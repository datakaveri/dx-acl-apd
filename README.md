[![Jenkins Build](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520ACL-APD%2520server%2520(v1.1.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20ACL-APD%20server%20(v1.1.0)%20pipeline/lastBuild/)
[![Jenkins Tests](https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520ACL-APD%2520server%2520(v1.1.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20ACL-APD%20server%20(v1.1.0)%20pipeline/lastBuild/testReport/)
[![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520ACL-APD%2520server%2520(v1.1.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20ACL-APD%20server%20(v1.1.0)%20pipeline/lastBuild/jacoco/)
[![Integration Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520ACL-APD%2520server%2520(v1.1.0)%2520pipeline%2F&label=integration%20tests)](https://jenkins.iudx.io/job/iudx%20ACL-APD%20server%20(v1.1.0)%20pipeline/lastBuild/Integration_20Test_20Report/)
[![Security Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520ACL-APD%2520server%2520(v1.1.0)%2520pipeline%2F&label=security%20tests)](https://jenkins.iudx.io/job/iudx%20ACL-APD%20server%20(v1.1.0)%20pipeline/lastBuild/zap/)

<p align="center">
<img src="./docs/cdpg.png" width="400">
</p>

# DX Access Control List (ACL) Access Policy Domain (APD) Server
## Introduction
The Data Exchange (DX) Access Control List (ACL) based Access Policy Domain (APD)
is used for creating, requesting and managing policy. Provider, provider delegates could
allow the consumer, consumer delegates to access their resources by writing a policy against it.
Policies are verified by Data Exchange (DX) Authentication Authorization and Accounting Server (AAA) Server to
allow consumer, consumer delegates to access the resource.

<p align="center">
<img src="./docs/acl-apd-overview.png">
</p>

## Features
The features of the DX ACL APD is as follows: 
- Allows provider, provider delegates to create, fetch, manage policies over their resources
- Allows consumers, consumer delegates to fetch policies, request access for resources 
- Emails are sent asynchronously using Vert.x SMTP Mail Client to the provider, provider delegates for resource access requests
- Integrates with DX AAA Server for token introspection to verify access before serving data to the designated user
- Integrates with AX Auditing server for logging and auditing the access for metering purposes
- Uses Vert.x, Postgres to create scalable, service mesh architecture

# Explanation
## Understanding ACL APD
- The section available [here](./docs/Solution_Architecture.md) explains the components/services used in implementing the ACL-APD server
- To try out the APIs, import the API collection, postman environment files in postman
- Reference : [postman-collection](src/main/resources/DX-ACL-APD.postman_collection.json), [postman-environment](src/main/resources/DX-ACL-APD.postman_environment.json)



[Watch Video 1](https://github.com/user-attachments/assets/3c142dd7-8596-4bd0-8bf3-49adcb3922fa)



# How To Guide
## Setup and Installation
Setup and Installation guide is available [here](./docs/SETUP-and-Installation.md)

# Tutorial
## Tutorials and Explanations
How to get access token

[Watch Video 1](https://github.com/user-attachments/assets/bc8aa7af-71a6-4623-8624-dae3e4964bd5)

[Watch Video 2](https://github.com/user-attachments/assets/abc909da-e470-4ce8-a8a1-0c7c11ccbbe1)


# Reference
## API Docs
API docs are available [here](https://redocly.github.io/redoc/?url=https://raw.githubusercontent.com/datakaveri/dx-acl-apd/main/docs/openapi.yaml)

## FAQ
FAQs are available [here](./docs/FAQ.md)


