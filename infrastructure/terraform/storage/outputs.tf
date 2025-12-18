# Storage Module Outputs

output "ecr_api_url" {
  description = "ECR repository URL for API"
  value       = aws_ecr_repository.api.repository_url
}

output "ecr_ui_url" {
  description = "ECR repository URL for UI"
  value       = aws_ecr_repository.ui.repository_url
}

output "ecr_registry" {
  description = "ECR registry URL"
  value       = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

