# GeoMeet Infrastructure - Outputs
# Output values for use by other configurations or scripts

# Networking Outputs
output "vpc_id" {
  description = "VPC ID"
  value       = module.networking.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.networking.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.networking.private_subnet_ids
}

# Database Outputs
output "db_endpoint" {
  description = "RDS database endpoint"
  value       = module.database.db_endpoint
  sensitive   = true
}

output "db_port" {
  description = "RDS database port"
  value       = module.database.db_port
}

# Compute Outputs
output "ec2_public_ip" {
  description = "EC2 instance public IP address"
  value       = module.compute.ec2_public_ip
}

output "ec2_public_dns" {
  description = "EC2 instance public DNS name"
  value       = module.compute.ec2_public_dns
}

# Storage Outputs
output "ecr_api_url" {
  description = "ECR repository URL for API"
  value       = module.storage.ecr_api_url
}

output "ecr_ui_url" {
  description = "ECR repository URL for UI"
  value       = module.storage.ecr_ui_url
}

output "ecr_registry" {
  description = "ECR registry URL"
  value       = module.storage.ecr_registry
}

# Security Outputs
output "ec2_security_group_id" {
  description = "EC2 security group ID"
  value       = module.security.ec2_security_group_id
}

output "database_security_group_id" {
  description = "Database security group ID"
  value       = module.security.database_security_group_id
}

