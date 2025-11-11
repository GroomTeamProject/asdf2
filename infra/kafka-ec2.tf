# ==========================================
# Kafka EC2 Instance
# ==========================================

# Create ENI - Kafka
resource "aws_network_interface" "kafka_eni" {
  subnet_id       = aws_subnet.team02_private_subnet_a.id
  private_ips     = ["10.0.3.100"]
  security_groups = [aws_security_group.kafka_security_group.id]

  tags = {
    Name = "team02-kafka-eni"
  }
}

# Create EC2 Instance - Kafka
resource "aws_instance" "kafka" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t3.small"
  key_name      = var.kafka_key_name

  network_interface {
    network_interface_id = aws_network_interface.kafka_eni.id
    device_index         = 0
  }

  # storage for Kafka data
  root_block_device {
    volume_size = 10
    volume_type = "gp3"
  }

  # initialization script
  user_data = file("${path.module}/kafka/kafka-ec2-init.sh")

  tags = {
    Name = "team02-kafka"
  }

  # upload kafka-compose.yml file
  provisioner "file" {
    source      = "${path.module}/kafka/kafka-compose.yml"
    destination = "/home/ubuntu/docker-compose.yml"

    connection {
      type                = "ssh"
      user                = "ubuntu"
      private_key         = file("~/.ssh/ec2_key_kafka.pem")
      host                = self.private_ip
      bastion_host        = aws_instance.bastion.public_ip
      bastion_user        = "ubuntu"
      bastion_private_key = file("~/.ssh/${var.bastion_key_name}.pem")
    }
  }

  # upload kafka-compose.service file
  provisioner "file" {
    source      = "${path.module}/kafka/kafka-compose.service"
    destination = "/home/ubuntu/kafka-compose.service"

    connection {
      type                = "ssh"
      user                = "ubuntu"
      private_key         = file("~/.ssh/ec2_key_kafka.pem")
      host                = self.private_ip
      bastion_host        = aws_instance.bastion.public_ip
      bastion_user        = "ubuntu"
      bastion_private_key = file("~/.ssh/${var.bastion_key_name}.pem")
    }
  }
}

# Create Security Group - Kafka
resource "aws_security_group" "kafka_security_group" {
  name        = "team02-kafka-security-group"
  description = "Security group for Kafka EC2"
  vpc_id      = aws_vpc.team02_vpc.id

  # allow Kafka
  ingress {
    from_port = 9092
    to_port   = 9092
    protocol  = "tcp"
    cidr_blocks = [
      aws_subnet.team02_private_subnet_a.cidr_block,
      aws_subnet.team02_private_subnet_b.cidr_block
    ]
    description = "Kafka from Private Subnets"
  }

  # allow Zookeeper
  ingress {
    from_port = 2181
    to_port   = 2181
    protocol  = "tcp"
    cidr_blocks = [
      aws_subnet.team02_private_subnet_a.cidr_block,
      aws_subnet.team02_private_subnet_b.cidr_block
    ]
    description = "Zookeeper from Private Subnets"
  }

  # allow Kafka UI (access via bastion)
  ingress {
    from_port       = 9001
    to_port         = 9001
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]
    description     = "Kafka UI from bastion"
  }

  # allow SSH from bastion
  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]
    description     = "SSH from bastion host"
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
