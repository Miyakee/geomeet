# GeoMeet Infrastructure

This directory contains the infrastructure as code (IaC) for the GeoMeet application, organized in a modular and maintainable structure.

## 📁 Directory Structure

```
infrastructure/
├── terraform/              # Terraform configuration files
│   ├── networking/        # Network resources (VPC, subnets, gateways)
│   ├── compute/          # Compute resources (EC2, ECS)
│   ├── database/         # Database resources (RDS)
│   ├── security/          # Security resources (security groups, IAM)
│   ├── storage/          # Storage resources (ECR)
│   └── main.tf           # Main Terraform configuration
├── scripts/              # Deployment and utility scripts
│   ├── deployment/       # Deployment scripts
│   ├── database/         # Database management scripts
│   └── ssl/              # SSL/HTTPS configuration scripts
├── config/               # Configuration files
│   ├── docker-compose.yml
│   └── nginx.conf
└── docs/                 # Documentation
    ├── deployment/
    ├── troubleshooting/
    └── architecture/
```

## 🚀 Quick Start

### Prerequisites

- Terraform >= 1.5.0
- AWS CLI configured with appropriate credentials
- Docker (for building images)

### Initial Setup

1. **Configure AWS credentials**:
   ```bash
   # Set your AWS profile name
   export AWS_PROFILE=your-aws-profile-name
   
   # Or use AWS SSO:
   aws sso login --profile your-aws-profile-name
   
   # Or configure via AWS CLI:
   aws configure --profile your-aws-profile-name
   ```

2. **Initialize Terraform**:
   ```bash
   cd terraform
   terraform init
   ```

3. **Plan infrastructure changes**:
   ```bash
   terraform plan
   ```

4. **Apply infrastructure**:
   ```bash
   terraform apply
   ```

## 📚 Documentation

- [Deployment Guide](docs/deployment/README.md)
- [Architecture Overview](docs/architecture/README.md)
- [Troubleshooting](docs/troubleshooting/README.md)

## 🏗️ Architecture

The infrastructure is organized into logical modules:

- **Networking**: VPC, subnets, internet gateway, route tables
- **Compute**: EC2 instances, ECS clusters (if needed)
- **Database**: RDS PostgreSQL instance
- **Security**: Security groups, IAM roles and policies
- **Storage**: ECR repositories for Docker images

## 🔧 Configuration

### Environment Variables

Set the following environment variables or update `terraform/variables.tf`:

- `AWS_REGION`: AWS region (default: `ap-southeast-1`)
- `AWS_PROFILE`: AWS profile name
- `ENVIRONMENT`: Environment name (default: `dev`)

### Terraform Variables

See `terraform/variables.tf` for all available variables.

## 📝 Deployment

See [Deployment Guide](docs/deployment/README.md) for detailed deployment instructions.

## 🔒 Security

- All resources are tagged with appropriate security tags
- Security groups follow least-privilege principles
- Database is in private subnet with restricted access
- IAM roles follow principle of least privilege

## 💰 Cost Optimization

- Uses cost-optimized instance types
- RDS instance uses minimal configuration for development
- ECR lifecycle policies to manage image storage

## 🧹 Cleanup

To destroy all infrastructure:

```bash
cd terraform
terraform destroy
```

**Warning**: This will delete all resources. Make sure you have backups if needed.

## 📞 Support

For issues or questions, refer to the troubleshooting guide or contact the infrastructure team.
