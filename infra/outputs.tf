# RDS Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.team02_mariadb.endpoint
}

output "mysql_connect_command" {
  description = "MySQL command to connect to RDS"
  value       = "mysql -h ${split(":", aws_db_instance.team02_mariadb.endpoint)[0]} -u ${var.db_username} -p"
}

output "rds_connection_string" {
  description = "JDBC connection string for Spring Boot"
  value       = "jdbc:mariadb://${aws_db_instance.team02_mariadb.endpoint}/{DB_NAME}?characterEncoding=UTF-8&useUnicode=true&serverTimezone=Asia/Seoul"
}

# Kafka Outputs  
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
  value       = "kafka.team02.local:9092"
}

output "kafka_ssh_command" {
  description = "SSH command to connect to Kafka EC2"
  value       = "ssh -i ~/.ssh/ec2_key_kafka.pem ubuntu@${aws_instance.kafka.public_ip}"
}

# Bastion Outputs
output "bastion_public_ip" {
  description = "Public IP of the bastion host"
  value       = aws_instance.bastion.public_ip
}

output "bastion_ssh_command" {
  description = "SSH command to connect to the bastion host"
  value       = "ssh -i ~/.ssh/${var.bastion_key_name}.pem ubuntu@${aws_instance.bastion.public_ip}"
}

# Gateway (API) Outputs
output "gateway_url" {
  description = "Gateway API external URL (LoadBalancer)"
  value       = "http://a11ecfb907f63449f9b6ae72179e43fe-996841244.ap-northeast-2.elb.amazonaws.com:8080"
}

output "gateway_loadbalancer_hostname" {
  description = "Gateway LoadBalancer hostname"
  value       = "a11ecfb907f63449f9b6ae72179e43fe-996841244.ap-northeast-2.elb.amazonaws.com"
}

output "gateway_api_domain" {
  description = "Gateway API domain name (if public_domain_name is set)"
  value       = var.public_domain_name != "" ? "http://api.${var.public_domain_name}:8080" : null
}
