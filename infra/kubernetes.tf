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

