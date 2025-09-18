output "api_url" {
  description = "API Gateway URL"
  value       = module.shared_infrastructure.api_url
}

output "api_id" {
  description = "API Gateway ID"
  value       = module.shared_infrastructure.api_id
}

output "global_table_name" {
  description = "Global table name"
  value       = module.shared_infrastructure.global_table_name
}

output "post_confirmation_function_arn" {
  description = "Post-confirmation function ARN"
  value       = module.cognito_integration_service.post_confirmation_function_arn
}