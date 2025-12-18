# VPC Configuration
# Virtual Private Cloud - Isolated network environment

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

