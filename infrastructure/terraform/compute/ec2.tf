# EC2 Instance Configuration

# Get latest Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Instance
resource "aws_instance" "app" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = var.instance_type

  # Key pair for SSH access
  key_name = var.key_name

  # Network configuration
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = [var.security_group_id]
  associate_public_ip_address = true

  # IAM role
  iam_instance_profile = var.iam_instance_profile_name

  # Storage
  root_block_device {
    volume_type = "gp3"
    volume_size = 30
    encrypted   = true
  }

  # User data script
  user_data = <<-EOF
    #!/bin/bash
    yum update -y
    
    # Install Docker
    yum install -y docker
    systemctl start docker
    systemctl enable docker
    usermod -a -G docker ec2-user
    
    # Install Docker Compose
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    # Stop system Nginx if running
    if systemctl is-active --quiet nginx 2>/dev/null; then
        systemctl stop nginx
        systemctl disable nginx
    fi
  EOF

  tags = {
    Name = "${var.project_name}-app-server"
  }
}

