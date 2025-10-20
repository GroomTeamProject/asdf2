resource "aws_security_group" "team02_security_group" {
  name        = "team02-security-group"
  description = "Allow HTTP"
  vpc_id      = aws_vpc.team02_vpc.id

  ingress {
    from_port   = 8085
    to_port     = 8085
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
