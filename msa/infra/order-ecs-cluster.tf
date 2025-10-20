resource "aws_ecs_cluster" "team02_cluster" {
  name = "team02-cluster"
}

resource "aws_ecs_task_definition" "order_service_task" {
  family                   = "order-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "order-service"
      image     = "docker.io/wsk221e/team02-order-service:latest"
      essential = true
      portMappings = [
        {
          containerPort = 8085
          hostPort      = 8085
        }
      ]
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "dev"
        }
      ]
    }
  ])
}

resource "aws_ecs_service" "order_service" {
  name            = "order-service"
  cluster         = aws_ecs_cluster.team02_cluster.id
  task_definition = aws_ecs_task_definition.order_service_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.team02_public_subnet_a.id]
    security_groups  = [aws_security_group.team02_security_group.id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.order_service.arn
  }
}

resource "aws_service_discovery_private_dns_namespace" "team02_namespace" {
  name = "team02.local"
  vpc  = aws_vpc.team02_vpc.id
}

resource "aws_service_discovery_service" "order_service" {
  name = "order-service"

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
