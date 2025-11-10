variable "db_username" {
  description = "Database administrator username"
  type        = string
  sensitive   = false
}

variable "db_password" {
  description = "Database administrator password"
  type        = string
  sensitive   = true
}

variable "bastion_key_name" {
  description = "EC2 key pair name for bastion host SSH access"
  type        = string
}

variable "bastion_allowed_cidrs" {
  description = "List of CIDR blocks allowed to SSH into the bastion host"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}
