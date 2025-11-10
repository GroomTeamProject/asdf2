data "aws_ami" "bastion" {
  most_recent = true
  owners      = ["099720109477"] # Canonical (Ubuntu)

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
}

resource "aws_instance" "bastion" {
  ami           = data.aws_ami.bastion.id
  instance_type = "t2.micro"

  subnet_id              = aws_subnet.team02_public_subnet_a.id
  vpc_security_group_ids = [aws_security_group.bastion.id]

  key_name                    = var.bastion_key_name
  associate_public_ip_address = true

  tags = {
    Name = "team02-bastion"
  }
}

