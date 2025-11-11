# ==========================================
# RDS - MariaDB
# ==========================================

# Create RDS Instance - MariaDB
resource "aws_db_instance" "team02_mariadb" {
  identifier     = "team02-mariadb"
  engine         = "mariadb"
  engine_version = "11.4"

  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  username = var.db_username
  password = var.db_password

  # encoding settings
  parameter_group_name = aws_db_parameter_group.team02_mariadb_params.name

  # network settings
  db_subnet_group_name   = aws_db_subnet_group.team02_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.team02_rds_security_group.id]

  # allow access only from VPC
  publicly_accessible = false

  # backup settings
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  # disable deletion protection (Dev env. only)
  skip_final_snapshot = true
  deletion_protection = false

  tags = {
    Name        = "team02-mariadb"
    Environment = "dev"
  }
}

# ==========================================
# RDS Parameter Group
# ==========================================

# Create Parameter Group - MariaDB
resource "aws_db_parameter_group" "team02_mariadb_params" {
  name   = "team02-mariadb-params"
  family = "mariadb11.4"

  parameter {
    name  = "collation_server"
    value = "utf8mb4_general_ci"
  }

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_client"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_connection"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_database"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_results"
    value = "utf8mb4"
  }

  tags = {
    Name = "team02-mariadb-params"
  }
}

# ==========================================
# RDS Subnet Group
# ==========================================

# Create Subnet Group - RDS
resource "aws_db_subnet_group" "team02_db_subnet_group" {
  name = "team02-db-subnet-group-v2"
  subnet_ids = [
    aws_subnet.team02_private_subnet_a.id,
    aws_subnet.team02_private_subnet_b.id
  ]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "team02-db-subnet-group"
  }
}

# ==========================================
# RDS Security Group
# ==========================================

# Create Security Group - RDS
resource "aws_security_group" "team02_rds_security_group" {
  name        = "team02-rds-security-group"
  description = "Security group for RDS MariaDB"
  vpc_id      = aws_vpc.team02_vpc.id

  # allow access only from VPC
  ingress {
    from_port = 3306
    to_port   = 3306
    protocol  = "tcp"
    security_groups = [
      aws_security_group.eks_node_group.id,
      aws_security_group.bastion.id
    ]
    description = "Allow access from EKS nodes and bastion"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "team02-rds-security-group"
  }
}
