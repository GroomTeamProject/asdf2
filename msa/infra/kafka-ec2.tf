# Kafka EC2 Security Group
resource "aws_security_group" "kafka_security_group" {
  name        = "team02-kafka-security-group"
  description = "Security group for Kafka EC2"
  vpc_id      = aws_vpc.team02_vpc.id

  # Kafka
  ingress {
    from_port   = 9092
    to_port     = 9092
    protocol    = "tcp"
    security_groups = [
      aws_security_group.team02_security_group.id
    ]
    description = "Kafka from ECS services"
  }

  # Zookeeper
  ingress {
    from_port   = 2181
    to_port     = 2181
    protocol    = "tcp"
    security_groups = [
      aws_security_group.team02_security_group.id
    ]
    description = "Zookeeper from ECS services"
  }

  # Kafka UI
  ingress {
    from_port   = 9001
    to_port     = 9001
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Kafka UI"
  }

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "team02-kafka-security-group"
  }
}

# Kafka EC2 Instance
resource "aws_instance" "kafka" {
  ami           = "ami-0c9c942bd7bf113a2"  # Ubuntu 22.04 LTS (ap-northeast-2)
  instance_type = "t3.small"  # 1 vCPU, 2GB RAM (트래픽 적음)
  key_name      = "ec2_key_kafka"  # AWS 콘솔에서 수동으로 생성 (필수!)
  
  subnet_id                   = aws_subnet.team02_public_subnet_a.id
  vpc_security_group_ids      = [aws_security_group.kafka_security_group.id]
  associate_public_ip_address = true

  # 스토리지 (Kafka 데이터용)
  root_block_device {
    volume_size = 30  # 30GB
    volume_type = "gp3"
  }

  # 별도 파일로 분리한 초기화 스크립트 사용
  user_data = file("${path.module}/kafka/kafka-ec2-init.sh")

  tags = {
    Name = "team02-kafka"
  }

  # kafka-compose.yml 파일 업로드
  provisioner "file" {
    source      = "${path.module}/kafka/kafka-compose.yml"
    destination = "/home/ubuntu/docker-compose.yml"

    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file("~/.ssh/ec2_key_kafka.pem")  # AWS에서 생성한 키
      host        = self.public_ip
    }
  }

  # kafka-compose.service 파일 업로드
  provisioner "file" {
    source      = "${path.module}/kafka/kafka-compose.service"
    destination = "/home/ubuntu/kafka-compose.service"

    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file("~/.ssh/ec2_key_kafka.pem")
      host        = self.public_ip
    }
  }
}

# Kafka DNS (Service Discovery 등록)
resource "aws_service_discovery_service" "kafka_service" {
  name = "kafka"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.team02_namespace.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# Kafka Instance를 Service Discovery에 수동 등록
resource "aws_service_discovery_instance" "kafka_instance" {
  instance_id = aws_instance.kafka.id
  service_id  = aws_service_discovery_service.kafka_service.id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.kafka.private_ip
  }
}

# Outputs
output "kafka_public_ip" {
  description = "Kafka EC2 Public IP"
  value       = aws_instance.kafka.public_ip
}

output "kafka_private_ip" {
  description = "Kafka EC2 Private IP"
  value       = aws_instance.kafka.private_ip
}

output "kafka_ui_url" {
  description = "Kafka UI URL"
  value       = "http://${aws_instance.kafka.public_ip}:9001"
}

output "kafka_bootstrap_servers" {
  description = "Kafka Bootstrap Servers (for ECS services)"
  value       = "kafka:9092"
}

output "kafka_ssh_command" {
  description = "SSH command to connect to Kafka EC2"
  value       = "ssh -i ~/.ssh/ec2_key_kafka.pem ubuntu@${aws_instance.kafka.public_ip}"
}

