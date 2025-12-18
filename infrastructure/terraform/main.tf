# GeoMeet Infrastructure - Main Configuration
# This file orchestrates all infrastructure modules

terraform {
  required_version = ">= 1.5.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional: Configure remote state backend
  # backend "s3" {
  #   bucket         = "geomeet-terraform-state"
  #   key            = "dev/terraform.tfstate"
  #   region         = "ap-southeast-1"
  #   encrypt        = true
  #   dynamodb_table = "geomeet-terraform-locks"
  # }
}

# Configure AWS Provider
provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile
  
  default_tags {
    tags = {
      Project     = "GeoMeet"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

# Networking Module
# VPC, Subnets, Internet Gateway, Route Tables
module "networking" {
  source = "./networking"
  
  vpc_cidr             = var.vpc_cidr
  availability_zones   = data.aws_availability_zones.available.names
  environment          = var.environment
  project_name         = var.project_name
}

# Security Module
# Security Groups, IAM Roles
module "security" {
  source = "./security"
  
  vpc_id              = module.networking.vpc_id
  public_subnet_ids   = module.networking.public_subnet_ids
  private_subnet_ids  = module.networking.private_subnet_ids
  environment         = var.environment
  project_name        = var.project_name
}

# Database Module
# RDS PostgreSQL Instance
module "database" {
  source = "./database"
  
  vpc_id                  = module.networking.vpc_id
  subnet_ids              = module.networking.private_subnet_ids
  security_group_id       = module.security.database_security_group_id
  db_instance_class       = var.db_instance_class
  db_allocated_storage    = var.db_allocated_storage
  db_name                 = var.db_name
  db_username             = var.db_username
  db_password             = var.db_password
  environment             = var.environment
  project_name            = var.project_name
  backup_retention_period = var.db_backup_retention_period
  backup_window           = var.db_backup_window
  maintenance_window      = var.db_maintenance_window
}

# Compute Module
# EC2 Instances
module "compute" {
  source = "./compute"
  
  vpc_id                    = module.networking.vpc_id
  subnet_id                 = module.networking.public_subnet_ids[0]
  security_group_id         = module.security.ec2_security_group_id
  instance_type             = var.ec2_instance_type
  key_name                  = var.ec2_key_name
  environment               = var.environment
  project_name              = var.project_name
  iam_instance_profile_name = module.security.ec2_instance_profile_name
}

# Storage Module
# ECR Repositories
module "storage" {
  source = "./storage"
  
  environment  = var.environment
  project_name = var.project_name
  aws_account_id = data.aws_caller_identity.current.account_id
  aws_region   = data.aws_region.current.name
}

