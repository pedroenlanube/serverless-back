output "api_id" {
  description = "API Gateway ID"
  value       = aws_apigatewayv2_api.main.id
}

output "api_url" {
  description = "API Gateway URL"
  value       = aws_apigatewayv2_stage.dev.invoke_url
}

output "api_execution_arn" {
  description = "API Gateway execution ARN"
  value       = aws_apigatewayv2_api.main.execution_arn
}

output "global_table_name" {
  description = "Main table name"
  value       = aws_dynamodb_table.main.name
}

output "global_table_arn" {
  description = "Main table ARN"
  value       = aws_dynamodb_table.main.arn
}

output "app_registry_application_arn" {
  description = "AppRegistry Application ARN"
  value       = aws_servicecatalogappregistry_application.main.arn
}

output "app_registry_application_id" {
  description = "AppRegistry Application ID"
  value       = aws_servicecatalogappregistry_application.main.id
}