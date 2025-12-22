# GeoMeet Infrastructure

This directory contains the infrastructure as code (IaC) and deployment scripts for the GeoMeet application, organized in a modular and maintainable structure.

## 📁 Directory Structure

```
infrastructure/
├── terraform/              # Terraform configuration files (Infrastructure as Code)
│   ├── networking/        # Network resources (VPC, subnets, gateways)
│   ├── compute/          # Compute resources (EC2 instances)
│   ├── database/         # Database resources (RDS PostgreSQL)
│   ├── security/          # Security resources (security groups, IAM)
│   ├── storage/          # Storage resources (ECR repositories)
│   └── main.tf           # Main Terraform configuration
├── scripts/              # Deployment and utility scripts
│   └── deployment/       # Deployment scripts
│       ├── build-and-push-to-ecr.sh    # Build and push Docker images to ECR
│       └── deploy-from-ecr.sh          # Deploy application from ECR to EC2
├── config/               # Configuration files
│   ├── docker-compose.yml              # Docker Compose configuration for production
│   ├── docker-compose.yml.example      # Example Docker Compose configuration
│   ├── nginx.conf                      # Nginx configuration with HTTPS
│   ├── nginx.conf.http-only            # Nginx configuration for SSL certificate initialization
│   └── init-once/                      # One-time initialization scripts
│       └── init-ssl.sh                 # SSL certificate initialization script
└── docs/                 # Documentation
    ├── deployment/
    ├── troubleshooting/
    └── architecture/
```

## 🏗️ Architecture Overview

The GeoMeet infrastructure is deployed on AWS using a modular architecture:

### Infrastructure Components

```
Internet
   │
   ▼
[EC2 Instance] (Public Subnet)
   │
   ├──► [Docker: API Container] (Port 8080)
   ├──► [Docker: Nginx/UI Container] (Ports 80, 443)
   └──► [Docker: Certbot Container] (SSL Certificate Management)
   │
   ▼
[RDS PostgreSQL] (Private Subnet)
```

### AWS Resources

- **Networking**:
  - VPC with public and private subnets
  - Internet Gateway for public access
  - Route tables for traffic routing

- **Compute**:
  - EC2 instance (Amazon Linux 2023) in public subnet
  - Docker and Docker Compose installed
  - IAM role for ECR access

- **Database**:
  - RDS PostgreSQL instance in private subnet
  - Encrypted storage
  - Automated backups

- **Storage**:
  - ECR repositories for Docker images (`geomeet-api`, `geomeet-ui`)

- **Security**:
  - Security groups for EC2 and RDS
  - IAM roles and policies
  - Encrypted storage and network traffic

## 🚀 Quick Start

### Prerequisites

- **Terraform** >= 1.5.0
- **AWS CLI** configured with appropriate credentials
- **Docker** (for building images locally)
- **Domain name** pointing to EC2 instance IP (for HTTPS)

### Step 1: Provision Infrastructure with Terraform

1. **Configure AWS credentials**:
   ```bash
   # Set your AWS profile name
   export AWS_PROFILE=your-aws-profile-name
   
   # Or use AWS SSO:
   aws sso login --profile your-aws-profile-name
   
   # Or configure via AWS CLI:
   aws configure --profile your-aws-profile-name
   ```

2. **Configure Terraform variables**:
   ```bash
   cd terraform
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your values
   ```

3. **Initialize Terraform**:
   ```bash
   terraform init
   ```

4. **Plan infrastructure changes**:
   ```bash
   terraform plan
   ```

5. **Apply infrastructure**:
   ```bash
   terraform apply
   ```

This will create:
- VPC with public/private subnets
- EC2 instance in public subnet
- RDS PostgreSQL in private subnet
- ECR repositories
- Security groups and IAM roles

### Step 2: Build and Push Docker Images to ECR

On your local machine or CI/CD:

```bash
cd infrastructure/scripts/deployment
./build-and-push-to-ecr.sh
```

This script:
- Builds API and UI Docker images
- Pushes images to ECR with `latest` and Git SHA tags
- Creates ECR repositories if they don't exist

**Configuration**:
- `AWS_REGION`: AWS region (default: `ap-southeast-1`)
- `AWS_PROFILE`: AWS profile name (default: `tw-esg`)

### Step 3: Deploy Application to EC2

SSH into your EC2 instance and run:

```bash
cd ~
git clone git@github.com:Miyakee/geomeet.git
cd geomeet/infrastructure/scripts/deployment
./deploy-from-ecr.sh
```

This script:
- Logs into ECR (using EC2 IAM role)
- Pulls latest images from ECR
- Clones/updates configuration files from GitHub
- Stops old containers
- Starts new containers with Docker Compose

**Configuration**:
- `AWS_REGION`: AWS region (default: `ap-southeast-1`)
- `AWS_ACCOUNT_ID`: AWS account ID (auto-detected)
- `IMAGE_TAG`: Image tag to deploy (default: `latest`)
- `DB_ENDPOINT`: RDS endpoint (auto-detected from Terraform)

### Step 4: Initialize SSL Certificate (One-time Setup)

On EC2 instance:

```bash
cd ~/geomeet
# Backup current nginx.conf
cp infrastructure/config/nginx.conf infrastructure/config/nginx.conf.bak

# Switch to HTTP-only config
cp infrastructure/config/nginx.conf.http-only infrastructure/config/nginx.conf

# Deploy with HTTP-only config
cd infrastructure/scripts/deployment
./deploy-from-ecr.sh

# Initialize SSL certificate
cd ~/geomeet/infrastructure/config/init-once
chmod +x init-ssl.sh
./init-ssl.sh

# Restore full HTTPS config
cp ~/geomeet/infrastructure/config/nginx.conf.bak ~/geomeet/infrastructure/config/nginx.conf

# Redeploy with HTTPS
cd ~/geomeet/infrastructure/scripts/deployment
./deploy-from-ecr.sh
```

**Note**: Update `DOMAIN` and `EMAIL` in `init-ssl.sh` before running.

## 📝 Deployment Workflow

### Development to Production Flow

```
1. Local Development
   ↓
2. Build Docker Images
   ↓
3. Push to ECR (build-and-push-to-ecr.sh)
   ↓
4. Deploy to EC2 (deploy-from-ecr.sh)
   ↓
5. Application Running
```

### Continuous Deployment

For automated deployments, integrate the scripts into your CI/CD pipeline:

```yaml
# Example GitHub Actions workflow
- name: Build and Push to ECR
  run: ./infrastructure/scripts/deployment/build-and-push-to-ecr.sh

- name: Deploy to EC2
  run: |
    ssh ec2-user@${{ secrets.EC2_HOST }} \
      "cd geomeet && git pull && \
       ./infrastructure/scripts/deployment/deploy-from-ecr.sh"
```

## 🔧 Configuration

### Environment Variables

Set the following environment variables on EC2:

```bash
# Database Configuration
export DB_ENDPOINT=your-rds-endpoint.region.rds.amazonaws.com
export DB_NAME=geomeet
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password

# Geocoding API Keys (Optional)
export OPENCAGE_API_KEY=your-opencage-api-key
export POSITIONSTACK_API_KEY=your-positionstack-api-key
```

Or create a `.env` file in the deployment directory:

```bash
cd ~/geomeet
mkdir -p .env
cat > .env << EOF
DB_ENDPOINT=your-rds-endpoint.region.rds.amazonaws.com
DB_NAME=geomeet
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
OPENCAGE_API_KEY=your-opencage-api-key
POSITIONSTACK_API_KEY=your-positionstack-api-key
EOF

# Load environment variables
set -a
source .env
set +a
```

### Terraform Variables

See `terraform/variables.tf` for all available variables. Key variables:

- `aws_region`: AWS region (default: `ap-southeast-1`)
- `aws_profile`: AWS profile name
- `environment`: Environment name (default: `dev`)
- `ec2_instance_type`: EC2 instance type (default: `t3.micro`)
- `ec2_key_name`: EC2 key pair name for SSH access
- `db_instance_class`: RDS instance class (default: `db.t3.micro`)
- `db_allocated_storage`: RDS storage size in GB (default: `20`)
- `db_username`: RDS master username
- `db_password`: RDS master password

## 🐳 Docker Compose Services

The production deployment uses Docker Compose with three services:

1. **api**: Spring Boot API container
   - Port: 8080
   - Health check: `/actuator/health`
   - Environment: `SPRING_PROFILES_ACTIVE=aws`

2. **nginx**: Nginx reverse proxy + React UI
   - Ports: 80 (HTTP), 443 (HTTPS)
   - Serves static files and proxies API requests
   - SSL certificates managed by Certbot

3. **certbot**: SSL certificate management
   - Automatic certificate renewal every 12 hours
   - Uses Let's Encrypt

## 🔒 Security

### Network Security

- **EC2 Security Group**: Allows inbound traffic on ports 22 (SSH), 80 (HTTP), 443 (HTTPS)
- **RDS Security Group**: Only allows inbound traffic from EC2 security group on port 5432
- **Private Subnet**: Database is in private subnet, not directly accessible from internet

### IAM Roles

- **EC2 Instance Role**: Grants permissions to:
  - Pull images from ECR
  - Access RDS (if using IAM authentication)
  - CloudWatch logs (optional)

### Data Security

- **Encrypted Storage**: RDS and EC2 volumes are encrypted
- **Encrypted Traffic**: HTTPS/TLS for all external communication
- **Secrets Management**: Database credentials stored as environment variables (consider AWS Secrets Manager for production)

## 💰 Cost Optimization

- **EC2**: Uses `t3.micro` instance type (eligible for free tier)
- **RDS**: Uses `db.t3.micro` instance type (eligible for free tier)
- **Storage**: Minimal storage allocation with auto-scaling
- **Single AZ**: Database in single AZ (can enable Multi-AZ for production)
- **ECR Lifecycle**: Configure lifecycle policies to clean up old images

## 📊 Monitoring

### Health Checks

- **API Health**: `https://your-domain.com/health`
- **Container Health**: Docker Compose health checks
- **RDS**: CloudWatch metrics (if enabled)

### Logs

View application logs:

```bash
# On EC2 instance
cd ~/geomeet
docker-compose logs -f api
docker-compose logs -f nginx
```

## 🔄 Updates and Maintenance

### Update Application

1. Build and push new images:
   ```bash
   ./infrastructure/scripts/deployment/build-and-push-to-ecr.sh
   ```

2. Deploy to EC2:
   ```bash
   ssh ec2-user@your-ec2-ip
   cd ~/geomeet/infrastructure/scripts/deployment
   ./deploy-from-ecr.sh
   ```

### Update Infrastructure

1. Modify Terraform files
2. Plan changes: `terraform plan`
3. Apply changes: `terraform apply`

### SSL Certificate Renewal

Certbot automatically renews certificates every 12 hours. Manual renewal:

```bash
cd ~/geomeet
docker-compose run --rm certbot renew
docker-compose restart nginx
```

## 🧹 Cleanup

### Destroy Infrastructure

To destroy all AWS resources:

```bash
cd terraform
terraform destroy
```

**Warning**: This will delete all resources including RDS database. Make sure you have backups if needed.

### Clean Up Docker Resources

On EC2 instance:

```bash
cd ~/geomeet
docker-compose down
docker system prune -a  # Remove unused images
```


## 📚 Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [AWS ECR Documentation](https://docs.aws.amazon.com/ecr/)
