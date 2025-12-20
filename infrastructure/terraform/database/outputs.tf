# Database Module Outputs

output "db_endpoint" {
  description = "RDS database endpoint (full endpoint with port)"
  # endpoint is a string in format "hostname:port", so we can use it directly
  value       = aws_db_instance.main.endpoint
  sensitive   = false
}

output "db_endpoint_address" {
  description = "RDS database endpoint address (hostname only)"
  # Extract hostname from endpoint string (format: "hostname:port")
  value       = split(":", aws_db_instance.main.endpoint)[0]
}

output "db_port" {
  description = "RDS database port"
  value       = aws_db_instance.main.port
}

output "db_name" {
  description = "Database name"
  value       = aws_db_instance.main.db_name
}

output "db_instance_id" {
  description = "RDS instance identifier"
  value       = aws_db_instance.main.id
}

