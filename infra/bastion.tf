# ==========================================
# Bastion Host
# ==========================================

# Create EC2 Instance - Bastion Host
resource "aws_instance" "bastion" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t2.micro"

  subnet_id              = aws_subnet.team02_public_subnet_a.id
  vpc_security_group_ids = [aws_security_group.bastion.id]

  key_name                    = var.bastion_key_name
  associate_public_ip_address = true

  tags = {
    Name = "team02-bastion"
  }
}

# ==========================================
# Bastion Security Group
# ==========================================

# Create Security Group - Bastion Host
resource "aws_security_group" "bastion" {
  name        = "team02-bastion-sg"
  description = "Bastion host SSH access"
  vpc_id      = aws_vpc.team02_vpc.id

  # allow SSH access
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.bastion_allowed_cidrs
    description = "SSH access to bastion"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "team02-bastion-sg"
  }
}