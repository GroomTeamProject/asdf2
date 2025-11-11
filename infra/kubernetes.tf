data "aws_eks_cluster" "team02" {
  name = aws_eks_cluster.team02_eks.name
}

data "aws_eks_cluster_auth" "team02" {
  name = aws_eks_cluster.team02_eks.name
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.team02.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.team02.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.team02.token
}

provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.team02.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.team02.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.team02.token
  }
}

resource "kubernetes_service_account" "aws_lb_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.aws_lb_controller.arn
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.aws_lb_controller
  ]
}

resource "kubernetes_namespace" "team02" {
  metadata {
    name = "team02"
  }
}

resource "kubernetes_secret" "app" {
  metadata {
    name      = "app-secrets"
    namespace = kubernetes_namespace.team02.metadata[0].name
  }

  data = jsondecode(data.aws_secretsmanager_secret_version.team02_secret.secret_string)

  depends_on = [
    kubernetes_namespace.team02
  ]
}

resource "helm_release" "aws_lb_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"

  set {
    name  = "clusterName"
    value = aws_eks_cluster.team02_eks.name
  }

  set {
    name  = "region"
    value = "ap-northeast-2"
  }

  set {
    name  = "vpcId"
    value = aws_vpc.team02_vpc.id
  }

  set {
    name  = "serviceAccount.create"
    value = "false"
  }

  set {
    name  = "serviceAccount.name"
    value = kubernetes_service_account.aws_lb_controller.metadata[0].name
  }

  depends_on = [
    kubernetes_service_account.aws_lb_controller,
    aws_iam_role_policy_attachment.aws_lb_controller
  ]
}

