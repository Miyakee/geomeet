# Networking Module

This module creates the network infrastructure for GeoMeet:

- VPC (Virtual Private Cloud)
- Public and private subnets across multiple availability zones
- Internet Gateway
- Route tables and associations

## Resources

- `aws_vpc`: Main VPC
- `aws_subnet`: Public and private subnets
- `aws_internet_gateway`: Internet gateway for public subnets
- `aws_route_table`: Route tables for public and private subnets
- `aws_route_table_association`: Associates subnets with route tables

## Outputs

- `vpc_id`: VPC ID
- `public_subnet_ids`: List of public subnet IDs
- `private_subnet_ids`: List of private subnet IDs

