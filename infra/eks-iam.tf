# ==========================================
# EKS Cluster IAM Role
# ==========================================

# Create IAM Role - EKS Cluster
resource "aws_iam_role" "eks_cluster_role" {
  name = "team02-eks-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = {
    Name = "team02-eks-cluster-role"
  }
}

# Attach IAM Role - EKS Cluster Policy
resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster_role.name
}

# Attach IAM Role - EKS VPC Resource Controller Policy
resource "aws_iam_role_policy_attachment" "eks_vpc_resource_controller" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSVPCResourceController"
  role       = aws_iam_role.eks_cluster_role.name
}

# ==========================================
# EKS Node Group IAM Role
# ==========================================

# Create IAM Role - EKS Node Group
resource "aws_iam_role" "eks_node_group_role" {
  name = "team02-eks-node-group-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = {
    Name = "team02-eks-node-group-role"
  }
}

# Attach IAM Role - EKS Worker Node Policy
resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_group_role.name
}

# Attach IAM Role - EKS CNI Policy
resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_group_role.name
}

# Attach IAM Role - EKS Container Registry Policy
resource "aws_iam_role_policy_attachment" "eks_container_registry_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_node_group_role.name
}

# ==========================================
# AWS Load Balancer Controller IAM Role
# ==========================================

locals {
  aws_lb_controller_sa_name      = "aws-load-balancer-controller"
  aws_lb_controller_sa_namespace = "kube-system"
  aws_lb_controller_oidc_url     = replace(aws_iam_openid_connect_provider.eks.url, "https://", "")
}

data "http" "aws_lb_controller_policy" {
  url = "https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.0/docs/install/iam_policy.json"
}

resource "aws_iam_policy" "aws_lb_controller" {
  name        = "AWSLoadBalancerControllerIAMPolicy"
  description = "IAM policy for AWS Load Balancer Controller"
  policy      = data.http.aws_lb_controller_policy.response_body

  tags = {
    Name = "AWSLoadBalancerControllerIAMPolicy"
  }
}

resource "aws_iam_role" "aws_lb_controller" {
  name = "team02-aws-load-balancer-controller-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.eks.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${local.aws_lb_controller_oidc_url}:sub" = "system:serviceaccount/${local.aws_lb_controller_sa_namespace}/${local.aws_lb_controller_sa_name}"
            "${local.aws_lb_controller_oidc_url}:aud" = "sts.amazonaws.com"
          }
        }
      }
    ]
  })

  tags = {
    Name = "team02-aws-load-balancer-controller-role"
  }
}

resource "aws_iam_role_policy_attachment" "aws_lb_controller" {
  role       = aws_iam_role.aws_lb_controller.name
  policy_arn = aws_iam_policy.aws_lb_controller.arn
}
