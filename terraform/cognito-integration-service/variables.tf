variable "api_id" {
  description = "API Gateway ID"
  type        = string
}

variable "api_execution_arn" {
  description = "API Gateway execution ARN"
  type        = string
}

variable "global_table_name" {
  description = "DynamoDB global table name"
  type        = string
}

variable "global_table_arn" {
  description = "DynamoDB global table ARN"
  type        = string
}



variable "environment" {
  description = "Environment name"
  type        = string
}

variable "project_name" {
  description = "Project name"
  type        = string
}