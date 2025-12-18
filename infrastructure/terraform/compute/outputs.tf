# Compute Module Outputs

output "ec2_public_ip" {
  description = "EC2 instance public IP address"
  value       = aws_instance.app.public_ip
}

output "ec2_public_dns" {
  description = "EC2 instance public DNS name"
  value       = aws_instance.app.public_dns
}

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

