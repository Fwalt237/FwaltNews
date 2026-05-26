variable "aws_region" {
  description = "The AWS region to deploy into"
  type        = string
  default     = "ca-central-1"
}

variable "project_name" {
  description = "The name of the project, used for tagging and naming resources"
  type        = string
  default     = "fwaltnews"
}

variable "vpc_cidr" {
  description = "The CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_username" {
  description = "The username for the PostgreSQL database"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "The password for the PostgreSQL database"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "The name of the database to create"
  type        = string
  default     = "fwaltnewsdb"
}

variable "jwt_secret" {
  description = "Secret key for signing JWT tokens"
  type = string
  sensitive = true
}

variable "gemini_api_key" {
  description = "API key for Google Gemini (AI model)"
  type = string
  sensitive = true
}

variable "github_client_id" {
  description = "OAuth2 client ID for GitHub login"
  type = string
  sensitive = true
}

variable "github_client_secret" {
  description = "OAuth2 client secret for GitHub login"
  type = string
  sensitive = true
}

variable "google_client_id" {
  description = "OAuth2 client ID for Google login"
  type = string
  sensitive = true
}

variable "google_client_secret" {
  description = "OAuth2 client secret for Google login"
  type = string
  sensitive = true
}

variable "newsdata_api_key" {
  description = "API key for Newsdata.io service"
  type = string
  sensitive = true
}