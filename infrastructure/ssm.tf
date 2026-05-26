resource "aws_ssm_parameter" "db_password" {
  name  = "/${var.project_name}/prod/db_password"
  type  = "SecureString"
  value = var.db_password
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "/${var.project_name}/prod/jwt_secret"
  type  = "SecureString"
  value = var.jwt_secret
}

resource "aws_ssm_parameter" "gemini_api_key" {
  name  = "/${var.project_name}/prod/gemini_api_key"
  type  = "SecureString"
  value = var.gemini_api_key
}

resource "aws_ssm_parameter" "github_client_id" {
  name  = "/${var.project_name}/prod/github_client_id"
  type  = "SecureString"
  value = var.github_client_id
}

resource "aws_ssm_parameter" "github_client_secret" {
  name  = "/${var.project_name}/prod/github_client_secret"
  type  = "SecureString"
  value = var.github_client_secret
}

resource "aws_ssm_parameter" "google_client_id" {
  name  = "/${var.project_name}/prod/google_client_id"
  type  = "SecureString"
  value = var.google_client_id
}

resource "aws_ssm_parameter" "google_client_secret" {
  name  = "/${var.project_name}/prod/google_client_secret"
  type  = "SecureString"
  value = var.google_client_secret
}

resource "aws_ssm_parameter" "newsdata_api_key" {
  name  = "/${var.project_name}/prod/newsdata_api_key"
  type  = "SecureString"
  value = var.newsdata_api_key
}