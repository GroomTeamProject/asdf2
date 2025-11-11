# ==========================================
# VPC
# ==========================================

# Create VPC
resource "aws_vpc" "team02_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "team02-vpc"
  }
}

# ==========================================
# Internet Gateway
# ==========================================

# Create Internet Gateway
# used for public subnet to access internet
resource "aws_internet_gateway" "team02_internet_gateway" {
  vpc_id = aws_vpc.team02_vpc.id

  tags = {
    Name = "team02-internet-gateway"
  }
}

# ==========================================
# Public Route Table
# ==========================================

# Create Public Route Table
# used for public subnet to access internet
resource "aws_route_table" "team02_public_route_table" {
  vpc_id = aws_vpc.team02_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.team02_internet_gateway.id
  }

  tags = {
    Name = "team02-public-route-table"
  }
}

# ==========================================
# Public Subnet A
# ==========================================

# Create Public Subnet A (AZ: AP-Northeast-2a)
resource "aws_subnet" "team02_public_subnet_a" {
  vpc_id            = aws_vpc.team02_vpc.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "ap-northeast-2a"
  map_public_ip_on_launch = true

  tags = {
    Name = "team02-public-a"
  }
}

# Create Route Table Association - Public Subnet A
resource "aws_route_table_association" "team02_public_a_association" {
  subnet_id      = aws_subnet.team02_public_subnet_a.id
  route_table_id = aws_route_table.team02_public_route_table.id
}

# ==========================================
# Public Subnet B
# ==========================================

# Create Public Subnet B (AZ: AP-Northeast-2b)
resource "aws_subnet" "team02_public_subnet_b" {
  vpc_id                  = aws_vpc.team02_vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "ap-northeast-2b"
  map_public_ip_on_launch = true

  tags = {
    Name = "team02-public-b"
  }
}

# Create Route Table Association - Public Subnet B
resource "aws_route_table_association" "team02_public_b_association" {
  subnet_id      = aws_subnet.team02_public_subnet_b.id
  route_table_id = aws_route_table.team02_public_route_table.id
}

# ==========================================
# Private Subnet A
# ==========================================

# Create NAT Elastic IP A
# used for NAT Gateway A to have a public IP
resource "aws_eip" "team02_nat_eip_a" {
  domain = "vpc"

  tags = {
    Name = "team02-nat-eip-a"
  }
}

# Create NAT Gateway A
# used for private subnet A to access internet
resource "aws_nat_gateway" "team02_nat_gateway_a" {
  allocation_id = aws_eip.team02_nat_eip_a.id
  subnet_id     = aws_subnet.team02_public_subnet_a.id

  tags = {
    Name = "team02-nat-gateway-a"
  }

  depends_on = [aws_internet_gateway.team02_internet_gateway]
}

# Create Private Route Table A
# used for private subnet A to access internet
resource "aws_route_table" "team02_private_route_table_a" {
  vpc_id = aws_vpc.team02_vpc.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.team02_nat_gateway_a.id
  }

  tags = {
    Name = "team02-private-route-table-a"
  }
}

# Create Private Subnet A (AZ: AP-Northeast-2a)
resource "aws_subnet" "team02_private_subnet_a" {
  vpc_id                  = aws_vpc.team02_vpc.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = false

  tags = {
    Name = "team02-private-a"
  }
}

# Create Route Table Association - Private Subnet A
resource "aws_route_table_association" "team02_private_a_association" {
  subnet_id      = aws_subnet.team02_private_subnet_a.id
  route_table_id = aws_route_table.team02_private_route_table_a.id
}

# ==========================================
# Private Subnet B
# ==========================================

# Create NAT Elastic IP B
# used for NAT Gateway B to have a public IP
resource "aws_eip" "team02_nat_eip_b" {
  domain = "vpc"

  tags = {
    Name = "team02-nat-eip-b"
  }
}

# Create NAT Gateway B
# used for private subnet B to access internet
resource "aws_nat_gateway" "team02_nat_gateway_b" {
  allocation_id = aws_eip.team02_nat_eip_b.id
  subnet_id     = aws_subnet.team02_public_subnet_b.id

  tags = {
    Name = "team02-nat-gateway-b"
  }

  depends_on = [aws_internet_gateway.team02_internet_gateway]
}

# Create Private Route Table B
# used for private subnet B to access internet
resource "aws_route_table" "team02_private_route_table_b" {
  vpc_id = aws_vpc.team02_vpc.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.team02_nat_gateway_b.id
  }

  tags = {
    Name = "team02-private-route-table-b"
  }
}

# Create Private Subnet B (AZ: AP-Northeast-2b)
resource "aws_subnet" "team02_private_subnet_b" {
  vpc_id                  = aws_vpc.team02_vpc.id
  cidr_block              = "10.0.4.0/24"
  availability_zone       = "ap-northeast-2b"
  map_public_ip_on_launch = false

  tags = {
    Name = "team02-private-b"
  }
}

# Create Route Table Association - Private Subnet B
resource "aws_route_table_association" "team02_private_b_association" {
  subnet_id      = aws_subnet.team02_private_subnet_b.id
  route_table_id = aws_route_table.team02_private_route_table_b.id
}

