# Architecture Documentation

This directory contains architecture documentation for the GeoMeet infrastructure.

## Overview

The GeoMeet infrastructure is deployed on AWS using:

- **Networking**: VPC with public and private subnets
- **Compute**: EC2 instances running Docker containers
- **Database**: RDS PostgreSQL in private subnet
- **Storage**: ECR for Docker images
- **Security**: Security groups and IAM roles

## Detailed Guides

- [Step-by-Step Guide](STEP_BY_STEP_GUIDE.md) - Comprehensive deployment guide for beginners

## Architecture Diagram

```
Internet
   │
   ▼
[ALB/EC2] ──► [EC2 Instance]
                │
                ├──► [Docker: API]
                ├──► [Docker: Nginx/UI]
                │
                ▼
            [RDS PostgreSQL]
```

